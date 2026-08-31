package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ForageEffect;
import com.github.laxika.magicalvibes.model.effect.ForageFollowUpEffect;
import com.github.laxika.magicalvibes.model.effect.ForageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ForageEffectHandler implements NormalEffectHandlerBean {

    private static final PermanentPredicate FOOD_FILTER =
            new PermanentHasSubtypePredicate(CardSubtype.FOOD);

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ForageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ForageEffect forage = (ForageEffect) effect;
        UUID controllerId = entry.getControllerId();
        boolean canExileGraveyardCards = gameData.playerGraveyards
                .getOrDefault(controllerId, List.of()).size() >= 3;
        boolean canSacrificeFood = gameData.playerBattlefields
                .getOrDefault(controllerId, List.of())
                .stream()
                .anyMatch(permanent -> gameQueryService.hasEffectiveSubtype(
                        gameData, permanent, CardSubtype.FOOD));

        if (!canExileGraveyardCards && !canSacrificeFood) {
            return;
        }

        CardEffect followUp = forage.thenEffect();
        if (followUp != null && followUp.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            followUp = new ForageFollowUpEffect(followUp);
        }
        CardEffect forageTrigger = followUp == null
                ? new ForageTriggerEffect()
                : SequenceEffect.of(new ForageTriggerEffect(), followUp);
        CardEffect exileCards = SequenceEffect.of(
                new ExileGraveyardCardsEffect(3, GraveyardExileScope.OWN), forageTrigger);
        CardEffect sacrificeFood = new SacrificePermanentThenEffect(
                FOOD_FILTER, forageTrigger, "a Food");

        if (canExileGraveyardCards && canSacrificeFood) {
            playerInputService.beginChooseModeChoice(
                    gameData,
                    controllerId,
                    entry.getCard(),
                    new ChooseOneEffect(List.of(
                            new ChooseOneEffect.ChooseOneOption(
                                    "Exile three cards from your graveyard.", exileCards),
                            new ChooseOneEffect.ChooseOneOption("Sacrifice a Food.", sacrificeFood))),
                    false,
                    entry.getSourcePermanentId());
            return;
        }

        int currentEffectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (currentEffectIndex < 0) {
            currentEffectIndex = findWrappedEffectIndex(entry, effect);
        }
        if (currentEffectIndex < 0) {
            throw new IllegalStateException("Forage effect is not part of the resolving stack entry");
        }

        entry.insertEffectsToResolve(
                currentEffectIndex + 1,
                List.of(canExileGraveyardCards ? exileCards : sacrificeFood));
    }

    private int findWrappedEffectIndex(StackEntry entry, CardEffect effect) {
        for (int i = 0; i < entry.getEffectsToResolve().size(); i++) {
            CardEffect candidate = entry.getEffectsToResolve().get(i);
            if (candidate instanceof MayEffect may && may.wrapped().equals(effect)) {
                return i;
            }
        }
        return -1;
    }
}
