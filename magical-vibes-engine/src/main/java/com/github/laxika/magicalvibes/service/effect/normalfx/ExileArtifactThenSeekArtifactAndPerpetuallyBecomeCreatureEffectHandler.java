package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileArtifactThenSeekArtifactAndPerpetuallyBecomeCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Cogwork Progenitor's mixed-zone exile, seek, and perpetual modification. */
@Component
@RequiredArgsConstructor
public class ExileArtifactThenSeekArtifactAndPerpetuallyBecomeCreatureEffectHandler
        implements NormalEffectHandlerBean {

    private static final CardTypePredicate ARTIFACT = new CardTypePredicate(CardType.ARTIFACT);

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileArtifactThenSeekArtifactAndPerpetuallyBecomeCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileThenSeek = (ExileArtifactThenSeekArtifactAndPerpetuallyBecomeCreatureEffect) effect;
        List<Candidate> candidates = matchingCandidates(gameData, entry);
        if (candidates.isEmpty()) {
            return;
        }
        if (candidates.size() == 1) {
            exileAndSeek(gameData, entry, exileThenSeek, candidates.getFirst());
            return;
        }

        playerInputService.beginArtifactPermanentOrGraveyardChoice(gameData,
                new PendingInteraction.ArtifactPermanentOrGraveyardCardChoice(
                        entry.getControllerId(),
                        candidates.stream().map(Candidate::cardId).toList(),
                        entry.getCard().getName() + " — choose another artifact to exile.",
                        exileThenSeek));
    }

    public void completeChoice(GameData gameData, StackEntry entry,
                               PendingInteraction.ArtifactPermanentOrGraveyardCardChoice interaction,
                               UUID chosenCardId) {
        Candidate chosen = matchingCandidates(gameData, entry).stream()
                .filter(candidate -> candidate.cardId().equals(chosenCardId))
                .findFirst()
                .orElse(null);
        if (chosen != null) {
            exileAndSeek(gameData, entry, interaction.effect(), chosen);
        }
    }

    private List<Candidate> matchingCandidates(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        UUID sourceCardId = entry.getCard() == null ? null : entry.getCard().getId();
        List<Candidate> candidates = new ArrayList<>();

        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(controllerId, List.of())) {
            if ((sourcePermanentId != null && sourcePermanentId.equals(permanent.getId()))
                    || (sourceCardId != null && sourceCardId.equals(permanent.getCard().getId()))) {
                continue;
            }
            if (gameQueryService.isArtifact(gameData, permanent)) {
                candidates.add(new Candidate(permanent.getCard().getId(), permanent, permanent.getCard()));
            }
        }

        for (Card card : gameData.playerGraveyards.getOrDefault(controllerId, List.of())) {
            if (predicateEvaluationService.matchesCardPredicate(
                    card, ARTIFACT, sourceCardId, gameData, controllerId)) {
                candidates.add(new Candidate(card.getId(), null, card));
            }
        }
        return candidates;
    }

    private void exileAndSeek(GameData gameData, StackEntry entry,
                               ExileArtifactThenSeekArtifactAndPerpetuallyBecomeCreatureEffect effect,
                               Candidate candidate) {
        UUID controllerId = entry.getControllerId();
        if (candidate.permanent() != null) {
            if (!permanentRemovalService.removePermanentToExile(gameData, candidate.permanent())) {
                return;
            }
        } else {
            permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, candidate.cardId());
            if (gameData.playerGraveyards.getOrDefault(controllerId, List.of()).stream()
                    .anyMatch(card -> card.getId().equals(candidate.cardId()))) {
                return;
            }
            exileService.exileCard(gameData, controllerId, candidate.card());
        }

        seekAndModify(gameData, entry, effect);
    }

    private void seekAndModify(GameData gameData, StackEntry entry,
                               ExileArtifactThenSeekArtifactAndPerpetuallyBecomeCreatureEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        List<Card> matchingCards = new ArrayList<>(library.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, ARTIFACT, null, gameData, controllerId))
                .toList());
        if (matchingCards.isEmpty()) {
            return;
        }

        Card sought = matchingCards.get(ThreadLocalRandom.current().nextInt(matchingCards.size()));
        gameData.perpetualCardTypes.merge(sought.getId(), Set.of(CardType.CREATURE, CardType.ARTIFACT),
                (existing, added) -> {
                    EnumSet<CardType> merged = EnumSet.noneOf(CardType.class);
                    merged.addAll(existing);
                    merged.addAll(added);
                    return Set.copyOf(merged);
                });
        gameData.perpetualCardSubtypes.merge(sought.getId(), Set.of(effect.subtype()),
                (existing, added) -> {
                    EnumSet<CardSubtype> merged = EnumSet.noneOf(CardSubtype.class);
                    merged.addAll(existing);
                    merged.addAll(added);
                    return Set.copyOf(merged);
                });
        gameData.perpetualCardBasePowerToughness.put(sought.getId(),
                new GameData.PerpetualBasePowerToughness(effect.power(), effect.toughness(),
                        gameData.nextTimestamp()));
        library.removeIf(card -> card.getId().equals(sought.getId()));
        gameData.addCardToHand(controllerId, sought);

        Card soughtForTriggers = sought;
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand != null) {
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).getId().equals(sought.getId())) {
                    Card modified = perpetuallyBecomeCreature(sought, effect);
                    hand.set(i, modified);
                    soughtForTriggers = modified;
                    break;
                }
            }
        }
        triggerCollectionService.checkSeekTriggers(gameData, controllerId, List.of(soughtForTriggers));
    }

    private Card perpetuallyBecomeCreature(Card card,
                                           ExileArtifactThenSeekArtifactAndPerpetuallyBecomeCreatureEffect effect) {
        Card copy = card.createRuntimeCopy();
        EnumSet<CardType> retainedTypes = EnumSet.noneOf(CardType.class);
        retainedTypes.add(copy.getType());
        retainedTypes.addAll(copy.getAdditionalTypes());
        retainedTypes.remove(CardType.CREATURE);
        retainedTypes.add(CardType.ARTIFACT);
        copy.setType(CardType.CREATURE);
        copy.setAdditionalTypes(retainedTypes);

        List<CardSubtype> subtypes = new ArrayList<>(copy.getSubtypes());
        if (!subtypes.contains(effect.subtype())) {
            subtypes.add(effect.subtype());
        }
        copy.setSubtypes(subtypes);
        copy.setPower(effect.power());
        copy.setToughness(effect.toughness());
        copy.freeze();
        return copy;
    }

    private record Candidate(UUID cardId, Permanent permanent, Card card) {
    }
}
