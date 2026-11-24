package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromOpponentCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromNonSubtypeCreaturesEffect;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionSharedByOwnCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromChosenPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsOfPermanentsYouControlEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantProtectionSharedByOwnCreaturesUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantProtectionSharedByOwnCreaturesUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return;
        }

        List<Permanent> creatures = battlefield.stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .toList();
        if (creatures.isEmpty()) {
            return;
        }

        Set<CardColor> protectionColors = EnumSet.noneOf(CardColor.class);
        Set<CardEffect> protectionEffects = new LinkedHashSet<>();
        for (Permanent creature : creatures) {
            collectProtectionAbilities(gameData, creature, protectionColors, protectionEffects);
        }
        if (!protectionColors.isEmpty()) {
            protectionEffects.add(new ProtectionFromColorsEffect(Set.copyOf(protectionColors)));
        }
        if (protectionEffects.isEmpty()) {
            return;
        }

        for (Permanent recipient : creatures) {
            for (CardEffect protection : protectionEffects) {
                gameData.addFloatingEffect(new FloatingContinuousEffect(
                        UUID.randomUUID(), entry.getCard().getName(), null, entry.getControllerId(),
                        new GrantEffectEffect(protection, Subjects.target()), recipient.getId(), null, null,
                        Duration.UNTIL_END_OF_TURN, 0));
            }
        }

        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" shares protection from among the creatures you control until end of turn.")
                .build());
        log.info("Game {} - {} shares protection among {} own creature(s)",
                gameData.id, entry.getCard().getName(), creatures.size());
    }

    private void collectProtectionAbilities(GameData gameData, Permanent creature,
                                            Set<CardColor> protectionColors,
                                            Set<CardEffect> protectionEffects) {
        GameQueryService.StaticBonus bonus = gameQueryService.computeStaticBonus(gameData, creature);
        protectionColors.addAll(bonus.protectionColors());

        if (!bonus.losesAllAbilities() && !gameQueryService.hasLostPrintedProtection(gameData, creature)) {
            for (CardEffect staticEffect : creature.getCard().getStaticEffects()) {
                if (staticEffect instanceof ProtectionFromColorsOfPermanentsYouControlEffect protection
                        && protection.subject() == null) {
                    collectColorsOfControlledPermanents(gameData, creature, protectionColors);
                } else if (staticEffect instanceof ProtectionFromChosenPlayerEffect
                        && !creature.getChosenPlayerIds().isEmpty()) {
                    // Protection from the chosen player is protection from that player (CR 702.16k).
                    protectionEffects.add(new ProtectionFromPlayersEffect(
                            Set.copyOf(creature.getChosenPlayerIds()), false));
                } else if (staticEffect instanceof ProtectionFromPermanentsMatchingEffect protection) {
                    protectionEffects.add(protection);
                } else if (staticEffect instanceof ProtectionGrantingEffect protection
                        && protection.protectionSubject() == null) {
                    addProtectionAbility(withChosenValues(protection, creature), protectionColors, protectionEffects);
                }
            }
        }
        for (CardEffect grantedEffect : bonus.grantedEffects()) {
            if (grantedEffect instanceof ProtectionGrantingEffect protection) {
                addProtectionAbility(withChosenValues(protection, creature), protectionColors, protectionEffects);
            } else if (grantedEffect instanceof ProtectionFromPermanentsMatchingEffect
                    || grantedEffect instanceof ProtectionFromNonSubtypeCreaturesEffect
                    || grantedEffect instanceof GrantProtectionFromOpponentCreaturesUntilEndOfTurnEffect) {
                protectionEffects.add(grantedEffect);
            }
        }
    }

    private void collectColorsOfControlledPermanents(GameData gameData, Permanent creature,
                                                     Set<CardColor> protectionColors) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        if (controllerId == null) return;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;
        for (Permanent permanent : battlefield) {
            protectionColors.addAll(gameQueryService.getEffectiveColors(gameData, permanent));
        }
    }

    /**
     * The protection ability with the creature's chosen odd/even quality or number fixed in it: "protection from each
     * mana value of the chosen quality" (Lavabrink Venturer) and "from each mana value other than the chosen number"
     * (Haktos the Unscarred) are protection from those mana values (CR 702.16i), so a creature that gains the ability
     * from this one has protection from the same mana values. {@code null} while nothing has been chosen.
     */
    private static ProtectionGrantingEffect withChosenValues(ProtectionGrantingEffect protection, Permanent creature) {
        if (protection.protectionFromManaValueParity() && protection.protectionFromFixedManaValueParity() == null) {
            return creature.getChosenManaValueParity() == null ? null
                    : ProtectionFromManaValueEffect.parity(creature.getChosenManaValueParity());
        }
        if (!protection.protectionFromManaValuesOtherThanChosen().isEmpty()
                && protection.protectionFromManaValuesOtherThanFixed() == null) {
            int chosen = creature.getChosenNumber();
            return protection.protectionFromManaValuesOtherThanChosen().contains(chosen)
                    ? ProtectionFromManaValueEffect.otherThan(chosen) : null;
        }
        return protection;
    }

    /**
     * Shares one protection ability: its colours join the shared colour protection, and any other quality it
     * protects from (CR 702.16a), colorless and players (CR 702.16k) included, is granted as the ability itself.
     */
    private void addProtectionAbility(ProtectionGrantingEffect protection,
                                      Set<CardColor> protectionColors,
                                      Set<CardEffect> protectionEffects) {
        if (protection == null) {
            return;
        }
        protectionColors.addAll(protection.protectionFromColors());
        if (!protection.protectionFromCardTypes().isEmpty()
                || !protection.protectionFromSubtypes().isEmpty()
                || protection.protectionFromManaValueAtLeast().isPresent()
                || protection.protectionFromManaValueAtMost().isPresent()
                || protection.protectionFromMulticolored()
                || protection.protectionFromEnemyColoredMulticolored()
                || protection.protectionFromMonocolored()
                || protection.protectionFromColoredSpells()
                || protection.protectionFromColorless()
                || !protection.protectionFromPlayers().isEmpty()
                || protection.protectionFromRingBearers()
                || protection.protectionFromModifiedCreatures()
                || protection.protectionFromManaValueParity()
                || !protection.protectionFromManaValuesOtherThanChosen().isEmpty()
                || protection.protectsFromEverything()) {
            protectionEffects.add(protection);
        }
    }
}
