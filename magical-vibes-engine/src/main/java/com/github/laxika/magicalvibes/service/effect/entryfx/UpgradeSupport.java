package com.github.laxika.magicalvibes.service.effect.entryfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.UpgradeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Shared entry-choice and overlay logic for the upgrade mechanic. */
@Component
@RequiredArgsConstructor
public class UpgradeSupport {

    private final GameQueryService gameQueryService;

    public boolean isUpgrade(Card card) {
        return card != null && card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(UpgradeEffect.class::isInstance);
    }

    /** Records the only possible covered artifact before the entering card is placed. */
    public void selectIfSingleArtifact(GameData gameData, UUID controllerId,
                                       Permanent enteringPermanent) {
        List<Permanent> artifacts = controlledArtifacts(gameData, controllerId, enteringPermanent);
        if (artifacts.size() == 1) {
            enteringPermanent.setChosenPermanentId(artifacts.getFirst().getId());
        }
    }

    public List<UUID> validArtifactIds(GameData gameData, UUID controllerId,
                                       Permanent enteringPermanent) {
        return controlledArtifacts(gameData, controllerId, enteringPermanent).stream()
                .map(Permanent::getId)
                .toList();
    }

    public boolean isValidCoveredArtifact(GameData gameData, UUID controllerId,
                                          Permanent enteringPermanent, Permanent covered) {
        return covered != null
                && !covered.getId().equals(enteringPermanent.getId())
                && gameData.playerBattlefields.getOrDefault(controllerId, List.of()).contains(covered)
                && gameQueryService.isArtifact(gameData, covered);
    }

    /** Overlays the entering card onto the covered permanent without creating an ETB event. */
    public void applyUpgrade(GameData gameData, UUID controllerId,
                             Permanent enteringPermanent, Permanent covered) {
        if (!isValidCoveredArtifact(gameData, controllerId, enteringPermanent, covered)) {
            throw new IllegalStateException("Chosen permanent is not a controlled artifact");
        }

        List<Card> coveredCards = new ArrayList<>(covered.cardsLeavingBattlefield());
        covered.getMeldComponentCards().clear();
        covered.getMeldComponentCards().add(enteringPermanent.getCard());
        covered.getMeldComponentCards().addAll(coveredCards);
        covered.setCard(enteringPermanent.getCard());
        covered.getPersistentGrantedKeywords().add(Keyword.HASTE);

        gameData.playerBattlefields.get(controllerId).removeIf(
                permanent -> permanent.getId().equals(enteringPermanent.getId()));
    }

    private List<Permanent> controlledArtifacts(GameData gameData, UUID controllerId,
                                                Permanent enteringPermanent) {
        return gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                .filter(permanent -> !permanent.getId().equals(enteringPermanent.getId()))
                .filter(permanent -> gameQueryService.isArtifact(gameData, permanent))
                .toList();
    }
}
