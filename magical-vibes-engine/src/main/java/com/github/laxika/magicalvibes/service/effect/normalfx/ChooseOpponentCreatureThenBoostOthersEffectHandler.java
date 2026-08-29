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

        if (creatureIds.isEmpty()) {
            return;
        }

        var context = new PermanentChoiceContext.ChooseOpponentCreatureThenBoostOthers(
                entry.getSourcePermanentId(), entry.getCard(), controllerId,
                choiceEffect.powerBoost(), choiceEffect.toughnessBoost());
        if (creatureIds.size() == 1) {
            completeChoice(gameData, creatureIds.getFirst(), context);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(context);
        playerInputService.beginPermanentChoice(gameData, controllerId, creatureIds,
                entry.getCard().getName() + " — Choose a creature an opponent controls.");
    }

    public void completeChoice(GameData gameData, UUID chosenPermanentId,
                               PermanentChoiceContext.ChooseOpponentCreatureThenBoostOthers context) {
        Permanent source = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenPermanentId);
        if (source == null || chosen == null) {
            return;
        }

        source.setChosenPermanentId(chosenPermanentId);
        var filter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                new PermanentNotPredicate(new PermanentIsSpecificPermanentPredicate(chosenPermanentId))
        ));
        boostAllCreaturesEffectHandler.resolve(gameData, new StackEntry(context.sourceCard(), context.controllerId()),
                new BoostAllCreaturesEffect(context.powerBoost(), context.toughnessBoost(), filter));
    }
}
