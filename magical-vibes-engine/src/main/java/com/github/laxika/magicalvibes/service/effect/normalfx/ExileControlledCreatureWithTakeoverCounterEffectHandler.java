package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileControlledCreatureWithTakeoverCounterEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves The Master's optional Body Thief cast trigger. */
@Component
@RequiredArgsConstructor
public class ExileControlledCreatureWithTakeoverCounterEffectHandler implements NormalEffectHandlerBean {

    private final ExileSupport exileSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileControlledCreatureWithTakeoverCounterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }

        List<UUID> creatureIds = battlefield.stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .map(Permanent::getId)
                .toList();
        if (creatureIds.isEmpty()) {
            return;
        }
        if (creatureIds.size() == 1) {
            completePermanentChoice(gameData, creatureIds.getFirst(),
                    new PermanentChoiceContext.TakeoverCreatureToExile(entry.getCard(), controllerId));
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.TakeoverCreatureToExile(entry.getCard(), controllerId));
        playerInputService.beginPermanentChoice(gameData, controllerId, creatureIds,
                entry.getCard().getName() + " — choose a creature to exile.");
    }

    public void completePermanentChoice(GameData gameData, UUID permanentId,
                                        PermanentChoiceContext.TakeoverCreatureToExile context) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (permanent == null
                || !context.controllerId().equals(gameQueryService.findPermanentController(gameData, permanentId))
                || !gameQueryService.isCreature(gameData, permanent)) {
            return;
        }

        Card card = permanent.getCard();
        exileSupport.exilePermanentAndLog(gameData, permanent, context.sourceCard().getName());
        if (gameData.findExiledCard(card.getId()) != null) {
            gameData.exiledCardsWithTakeoverCounters.add(card.getId());
        }
    }
}
