package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastInvokeCalamityChosenSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.service.input.MayCastHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.github.laxika.magicalvibes.service.effect.normalfx.CastUpToTwoInstantOrSorceriesFromHandOrGraveyardWithoutPayingManaCostEffectHandler.MAX_TOTAL_MANA_VALUE;

/** Routes an Invoke Calamity selection to the normal free-cast implementation for its zone. */
@Component
@RequiredArgsConstructor
public class CastInvokeCalamityChosenSpellHandler implements MayEffectHandlerBean {

    private final MayCastHandlerService mayCastHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastInvokeCalamityChosenSpellEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        CastInvokeCalamityChosenSpellEffect effect = ability.effects().stream()
                .filter(CastInvokeCalamityChosenSpellEffect.class::isInstance)
                .map(CastInvokeCalamityChosenSpellEffect.class::cast)
                .findFirst()
                .orElseThrow();
        int maxManaValue = MAX_TOTAL_MANA_VALUE - effect.reservedManaValue()
                - earlierSpellsManaValue(gameData, effect);
        if (effect.fromGraveyard()) {
            mayCastHandlerService.handleCastFromGraveyardChoice(
                    gameData, player, accepted, ability,
                    new CastTargetInstantOrSorceryFromGraveyardEffect(
                            GraveyardSearchScope.CONTROLLERS_GRAVEYARD, true, true),
                    maxManaValue);
        } else {
            mayCastHandlerService.handleMayCastFromHandWithoutPaying(
                    gameData, player, accepted, ability, null, false, false, maxManaValue);
        }
    }

    /**
     * The total mana value of the spells the earlier selections put on the stack. All of them are
     * cast while Invoke Calamity resolves, so each one cast is still on the stack, with only the
     * half that was cast (CR 709.3b). A selection that wasn't cast spends nothing.
     */
    private static int earlierSpellsManaValue(GameData gameData, CastInvokeCalamityChosenSpellEffect effect) {
        return gameData.stack.stream()
                .map(StackEntry::getCard)
                .filter(card -> effect.earlierSelectedIds().contains(card.getId()))
                .mapToInt(card -> card.getManaValue())
                .sum();
    }
}
