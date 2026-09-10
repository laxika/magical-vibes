package com.github.laxika.magicalvibes.service.planar;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.networking.model.PlanechaseView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class PlanechaseViewService {
    private final CardViewFactory cards;
    private final PlanechaseService planechase;
    private final PlanarAbilityService abilities;

    public PlanechaseViewService(CardViewFactory cards, PlanechaseService planechase, PlanarAbilityService abilities) {
        this.abilities = abilities;
        this.cards = cards;
        this.planechase = planechase;
    }

    public PlanechaseView create(GameData game, UUID playerId) {
        if (game.planechase == null) return null;
        var state = game.planechase;
        UUID actorId = playerId != null && playerId.equals(game.mindControllerPlayerId)
                ? game.mindControlledPlayerId
                : playerId != null && playerId.equals(game.mindControlledPlayerId) ? null : playerId;
        boolean canRoll = actorId != null && planechase.canOfferRoll(game, actorId);
        return new PlanechaseView(state.faceUp.stream().map(object ->
                new PlanechaseView.PlanarCardView(object.getId(), cards.create(object.getCard()),
                        Map.copyOf(object.getCounters()), java.util.stream.IntStream.range(0, object.getCard().getActivatedAbilities().size())
                                .filter(index -> actorId != null && abilities.available(game, actorId, object,
                                        object.getCard().getActivatedAbilities().get(index)))
                                .boxed().toList())).toList(),
                state.controllerId, state.deck.size(), state.rollCost(game.activePlayerId, game.turnNumber),
                canRoll, canRoll && planechase.canRoll(game, actorId, game.playerManaPools.get(actorId)),
                state.lastRoll, state.lastRollPlayerId, state.rollSequence);
    }
}
