package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapTwoCreaturesSharingTypeCost;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles {@link TapTwoCreaturesSharingTypeCost} — "Tap two untapped creatures you control that
 * share a creature type" (Weight of Conscience). The two creatures must share a creature type with
 * each other (Changeling-aware, CR 702.73), a mutual constraint the plain multi-tap filter cannot
 * express.
 *
 * <p>Payment is resolved one choice at a time. {@code alreadyChosen} tracks the creatures tapped so
 * far for this cost — seeded from the pending-choice context across async prompts, and grown in place
 * during a synchronous auto-pay loop (where the same handler instance pays every choice). The first
 * pick must be part of some sharing pair among the controller's untapped creatures; each subsequent
 * pick must share a creature type with the first-chosen creature.</p>
 */
public class TapTwoSharingCreatureTypeCostHandler implements PermanentChoiceCostHandler {

    private final TapTwoCreaturesSharingTypeCost cost;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final TriggerCollectionService triggerCollectionService;
    private final List<UUID> alreadyChosen;

    public TapTwoSharingCreatureTypeCostHandler(TapTwoCreaturesSharingTypeCost cost,
                                                GameQueryService gameQueryService,
                                                GameBroadcastService gameBroadcastService,
                                                TriggerCollectionService triggerCollectionService,
                                                List<UUID> alreadyChosen) {
        this.cost = cost;
        this.gameQueryService = gameQueryService;
        this.gameBroadcastService = gameBroadcastService;
        this.triggerCollectionService = triggerCollectionService;
        this.alreadyChosen = new ArrayList<>(alreadyChosen == null ? List.of() : alreadyChosen);
    }

    @Override public CardEffect costEffect() { return cost; }
    @Override public int requiredCount() { return 2; }

    @Override
    public void validateCanPay(GameData gameData, UUID playerId) {
        if (getValidChoiceIds(gameData, playerId).size() < 2) {
            throw new IllegalStateException("Need two untapped creatures you control that share a creature type");
        }
    }

    @Override
    public List<UUID> getValidChoiceIds(GameData gameData, UUID playerId) {
        List<Permanent> untappedCreatures = untappedCreatures(gameData, playerId);

        if (!alreadyChosen.isEmpty()) {
            // Subsequent pick: must share a creature type with the first creature already tapped.
            Permanent anchor = gameQueryService.findPermanentById(gameData, alreadyChosen.getFirst());
            if (anchor == null) return List.of();
            return untappedCreatures.stream()
                    .filter(p -> sharesCreatureType(anchor, p))
                    .map(Permanent::getId)
                    .toList();
        }

        // First pick: must be part of at least one sharing pair among untapped creatures.
        return untappedCreatures.stream()
                .filter(p -> untappedCreatures.stream().anyMatch(o -> o != p && sharesCreatureType(p, o)))
                .map(Permanent::getId)
                .toList();
    }

    @Override
    public void validateAndPay(GameData gameData, Player player, Permanent chosen) {
        if (chosen.isTapped()) {
            throw new IllegalStateException("Creature is already tapped");
        }
        if (!gameQueryService.isCreature(gameData, chosen)) {
            throw new IllegalStateException("Must tap a creature");
        }
        if (alreadyChosen.isEmpty()) {
            boolean pairExists = untappedCreatures(gameData, player.getId()).stream()
                    .anyMatch(o -> o != chosen && sharesCreatureType(chosen, o));
            if (!pairExists) {
                throw new IllegalStateException("The two creatures must share a creature type");
            }
        } else {
            Permanent anchor = gameQueryService.findPermanentById(gameData, alreadyChosen.getFirst());
            if (anchor == null || !sharesCreatureType(anchor, chosen)) {
                throw new IllegalStateException("The two creatures must share a creature type");
            }
        }
        chosen.tap();
        triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, chosen);
        alreadyChosen.add(chosen.getId());
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(player.getUsername() + " taps " , chosen.getCard(), " as a cost."));
    }

    @Override
    public String getPromptMessage(int remaining) {
        return "Choose an untapped creature to tap that shares a creature type ("
                + remaining + " remaining).";
    }

    private List<Permanent> untappedCreatures(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        return battlefield.stream()
                .filter(p -> !p.isTapped())
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .toList();
    }

    private boolean sharesCreatureType(Permanent a, Permanent b) {
        List<CardSubtype> typesA = new ArrayList<>(a.getCard().getSubtypes());
        typesA.addAll(a.getTransientSubtypes());
        boolean aIsChangeling = a.hasKeyword(Keyword.CHANGELING);

        List<CardSubtype> typesB = new ArrayList<>(b.getCard().getSubtypes());
        typesB.addAll(b.getTransientSubtypes());
        boolean bIsChangeling = b.hasKeyword(Keyword.CHANGELING);

        return (aIsChangeling && (bIsChangeling || !typesB.isEmpty()))
                || (bIsChangeling && !typesA.isEmpty())
                || typesA.stream().anyMatch(typesB::contains);
    }
}
