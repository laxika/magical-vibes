package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.BattlefieldEntryCard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetThenSearchLibraryPutBothOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryBatchSupport;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Exiles the target, searches the library, and puts the exiled card and the found card onto the
 * battlefield together. The exiled card is carried through the library search so that both enter
 * at the same time; when the search opens no prompt (nothing to find, empty library) the exiled card
 * still returns on its own. Auras, copy-on-enter permanents, tokens and merged or melded permanents
 * are flickered the ordinary way before the search.
 */
@Component
@RequiredArgsConstructor
public class ExileTargetThenSearchLibraryPutBothOntoBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;
    private final FlickerEffectHandler flickerEffectHandler;
    private final SearchLibraryEffectHandler searchLibraryEffectHandler;
    private final BattlefieldEntryBatchSupport battlefieldEntryBatchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetThenSearchLibraryPutBothOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTargetThenSearchLibraryPutBothOntoBattlefieldEffect) effect;
        SearchLibraryEffect search = new SearchLibraryEffect(e.searchFilter(), LibrarySearchDestination.BATTLEFIELD);

        List<UUID> targets = entry.targetsForEffect(effect);
        UUID targetId = !targets.isEmpty() ? targets.getFirst() : entry.getTargetId();
        Permanent target = targetId == null ? null : gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            searchLibraryEffectHandler.resolve(gameData, entry, search);
            return;
        }

        List<Card> leaving = target.cardsLeavingBattlefield();
        Card card = target.getOriginalCard();
        if (leaving.size() != 1 || card.isToken() || card.isAura()
                || card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(CopyPermanentOnEnterEffect.class::isInstance)) {
            flickerEffectHandler.resolve(gameData, entry, FlickerEffect.flickerTargetUnderYourControl());
            StackEntry parked = gameData.pendingEffectResolutionEntry;
            if (gameData.interaction.isAwaitingInput() && parked != null) {
                parked.insertEffectsToResolve(gameData.pendingEffectResolutionIndex, List.of(search));
            } else {
                searchLibraryEffectHandler.resolve(gameData, entry, search);
            }
            return;
        }

        UUID controllerId = entry.getControllerId();
        UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(),
                card.getOwnerId() != null ? card.getOwnerId() : controllerId);
        if (!permanentRemovalService.removePermanentToExile(gameData, target)) {
            searchLibraryEffectHandler.resolve(gameData, entry, search);
            return;
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        if (gameData.findExiledCard(card.getId()) == null) {
            searchLibraryEffectHandler.resolve(gameData, entry, search);
            return;
        }

        searchLibraryEffectHandler.resolveWithCardsEnteringTogether(gameData, entry, search, List.of(card));

        boolean stillWaiting = gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class) != null;
        if (!stillWaiting && gameData.findExiledCard(card.getId()) != null) {
            gameLogService.append(gameData, GameLog.builder().card(card).text(" returns to the battlefield.").build());
            battlefieldEntryBatchSupport.begin(gameData, List.of(
                    new BattlefieldEntryCard(controllerId, ownerId, card, Zone.EXILE, null)));
        }
    }
}
