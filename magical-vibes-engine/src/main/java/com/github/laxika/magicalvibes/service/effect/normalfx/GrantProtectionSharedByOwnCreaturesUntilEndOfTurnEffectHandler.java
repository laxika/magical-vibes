package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionSharedByOwnCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsOfPermanentsYouControlEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionGrantingEffect;
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
                        new GrantEffectEffect(protection, GrantScope.TARGET), recipient.getId(), null, null,
                        EffectDuration.UNTIL_END_OF_TURN, 0));
            }
            recipient.getProtectionFromNonSubtypeCreaturesUntilEndOfTurn()
                    .addAll(creatures.stream()
                            .flatMap(source -> source.getProtectionFromNonSubtypeCreaturesUntilEndOfTurn().stream())
                            .toList());
            if (creatures.stream().anyMatch(Permanent::isProtectionFromOpponentCreaturesUntilEndOfTurn)) {
                recipient.setProtectionFromOpponentCreaturesUntilEndOfTurn(true);
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
        protectionColors.addAll(creature.getProtectionFromColorsUntilEndOfTurn());

        if (!bonus.losesAllAbilities()) {
            for (CardEffect staticEffect : creature.getCard().getEffects(EffectSlot.STATIC)) {
                if (staticEffect instanceof ProtectionFromColorsOfPermanentsYouControlEffect protection
                        && protection.scope() == null) {
                    collectColorsOfControlledPermanents(gameData, creature, protectionColors);
                } else if (staticEffect instanceof ProtectionGrantingEffect protection
                        && protection.protectionScope() == null) {
                    addProtectionAbility(protection, protectionColors, protectionEffects);
                }
            }
        }
        for (CardEffect grantedEffect : bonus.grantedEffects()) {
            if (grantedEffect instanceof ProtectionGrantingEffect protection) {
                addProtectionAbility(protection, protectionColors, protectionEffects);
            }
        }

        if (!creature.getProtectionFromCardTypes().isEmpty()) {
            protectionEffects.add(new ProtectionFromCardTypesEffect(
                    Set.copyOf(creature.getProtectionFromCardTypes())));
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

    private void addProtectionAbility(ProtectionGrantingEffect protection,
                                      Set<CardColor> protectionColors,
                                      Set<CardEffect> protectionEffects) {
        protectionColors.addAll(protection.protectionFromColors());
        if (!protection.protectionFromCardTypes().isEmpty()
                || !protection.protectionFromSubtypes().isEmpty()
                || protection.protectionFromManaValueAtLeast().isPresent()
                || protection.protectionFromMulticolored()
                || protection.protectionFromColoredSpells()
                || protection.protectsFromEverything()) {
            protectionEffects.add(protection);
        }
    }
}
