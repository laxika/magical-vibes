package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentCreatureThenBoostOthersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChooseOpponentCreatureThenBoostOthersEffectHandler implements NormalEffectHandlerBean {

    private final BoostAllCreaturesEffectHandler boostAllCreaturesEffectHandler;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseOpponentCreatureThenBoostOthersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choiceEffect = (ChooseOpponentCreatureThenBoostOthersEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<UUID> creatureIds = new ArrayList<>();

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) {
                continue;
            }
            for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    creatureIds.add(permanent.getId());
                }
            }
        }

        var context = new PermanentChoiceContext.ChooseOpponentCreatureThenBoostOthers(
                entry.getSourcePermanentId(), entry.getCard(), controllerId,
                choiceEffect.powerBoost(), choiceEffect.toughnessBoost());
        if (creatureIds.isEmpty()) {
            completeChoice(gameData, null, context);
            return;
        }
        if (creatureIds.size() == 1) {
            completeChoice(gameData, creatureIds.getFirst(), context);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(context);
        playerInputService.beginPermanentChoice(gameData, controllerId, creatureIds,
                entry.getCard().getName() + " — Choose a creature an opponent controls.");
    }

    /**
     * Applies the boost to every other creature, excluding the source card and, when one was
     * chosen, the chosen creature. The ability resolves even if its source has already left the
     * battlefield, and with no chosen creature (none an opponent controls) the boost still applies.
     */
    public void completeChoice(GameData gameData, UUID chosenPermanentId,
                               PermanentChoiceContext.ChooseOpponentCreatureThenBoostOthers context) {
        Permanent source = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
        if (source != null && chosenPermanentId != null) {
            source.setChosenPermanentId(chosenPermanentId);
        }
        List<PermanentPredicate> predicates = new ArrayList<>(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
        if (chosenPermanentId != null) {
            predicates.add(new PermanentNotPredicate(new PermanentIsSpecificPermanentPredicate(chosenPermanentId)));
        }
        var filter = new PermanentAllOfPredicate(predicates);
        boostAllCreaturesEffectHandler.resolve(gameData, new StackEntry(context.sourceCard(), context.controllerId()),
                new BoostAllCreaturesEffect(context.powerBoost(), context.toughnessBoost(), filter));
    }
}
