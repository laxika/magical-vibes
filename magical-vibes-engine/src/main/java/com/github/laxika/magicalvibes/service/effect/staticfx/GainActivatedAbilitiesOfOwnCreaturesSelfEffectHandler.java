package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfOwnCreaturesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The ability borrowers ({@link GainActivatedAbilitiesOfOwnCreaturesEffect}): the source has the activated abilities
 * of each other permanent the effect names, read through {@link #effectiveActivatedAbilities}.
 */
@Component
@RequiredArgsConstructor
public class GainActivatedAbilitiesOfOwnCreaturesSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainActivatedAbilitiesOfOwnCreaturesEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect,
                      StaticBonusAccumulator accumulator) {
        GainActivatedAbilitiesOfOwnCreaturesEffect gain = (GainActivatedAbilitiesOfOwnCreaturesEffect) effect;
        GameData gameData = context.gameData();
        if (context.sourceControllerId() == null) return;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                // The source's own abilities are already its own.
                if (permanent.getId().equals(context.sourceId())
                        || !support.namesOnBattlefieldOf(context, permanent, playerId, gain.permanents())
                        || gain.filter() != null && !support.matchesStaticFilter(context, permanent, gain.filter())) {
                    continue;
                }
                for (ActivatedAbility ability : effectiveActivatedAbilities(gameData, permanent, gameQueryService)) {
                    if (gain.abilities().includes(ability)) {
                        accumulator.addActivatedAbility(ability);
                    }
                }
            }
        }
    }

    /**
     * The activated abilities a permanent currently supplies to an ability borrower: its card's abilities and the ones
     * granted to it, without the card's when it is face down (CR 708.2a) or has lost them (all abilities, or a land's
     * printed abilities as it became a basic land type), only the card's mana abilities when it lost all other
     * abilities (Blood Sun), and the intrinsic mana abilities of its basic land types (CR 305.6).
     *
     * <p>A permanent whose own abilities are being assembled further up this thread (two borrowers lending to each
     * other, Marvin and Robaran Mercenaries, two Drana and Linvala) supplies only its card's abilities: what it
     * borrows itself isn't known yet, and asking would loop.
     */
    public static List<ActivatedAbility> effectiveActivatedAbilities(GameData gameData, Permanent permanent,
                                                                    GameQueryService gameQueryService) {
        if (GameQueryService.isAssemblingStaticBonus(permanent.getId())) {
            return permanent.isFaceDown() || gameQueryService.hasLostPrintedAbilities(gameData, permanent)
                    ? List.of() : List.copyOf(permanent.getCard().getActivatedAbilities(Zone.BATTLEFIELD));
        }
        GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, permanent);
        List<ActivatedAbility> abilities = new ArrayList<>();
        if (staticBonus.losesAllAbilities() || gameData.losesAllAbilitiesByEffect(permanent)
                || permanent.isFaceDown() || !staticBonus.losesAllNonManaAbilities()
                && gameQueryService.hasLostPrintedAbilities(gameData, permanent)) {
            abilities.addAll(staticBonus.grantedActivatedAbilities());
        } else if (staticBonus.losesAllNonManaAbilities()) {
            abilities.addAll(permanent.getCard().getActivatedAbilities(Zone.BATTLEFIELD).stream()
                    .filter(ActivatedAbility::isManaAbility)
                    .toList());
            abilities.addAll(staticBonus.grantedActivatedAbilities());
        } else {
            abilities.addAll(permanent.getCard().getActivatedAbilities(Zone.BATTLEFIELD));
            abilities.addAll(staticBonus.grantedActivatedAbilities());
        }
        abilities.addAll(gameQueryService.missingIntrinsicManaAbilities(gameData, permanent, abilities));
        return abilities;
    }
}
