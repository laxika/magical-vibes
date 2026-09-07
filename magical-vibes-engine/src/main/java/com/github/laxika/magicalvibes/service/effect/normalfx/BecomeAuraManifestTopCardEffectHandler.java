package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeAuraManifestTopCardEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Lightform's Aura conversion and manifest ability. */
@Slf4j
@Component
@RequiredArgsConstructor
public class BecomeAuraManifestTopCardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeAuraManifestTopCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent aura = findSource(gameData, entry);
        if (aura == null) {
            return;
        }

        becomeAura(aura);

        List<Card> library = gameData.playerDecks.get(entry.getControllerId());
        if (library == null || library.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    " becomes an Aura, but its controller's library is empty."));
            return;
        }

        Card manifestedCard = library.removeFirst();
        Permanent manifested = new Permanent(manifestedCard);
        manifested.setManifested(true);
        manifested.setFaceDown(2, 2, java.util.Set.of(CardType.CREATURE));
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, entry.getControllerId(), manifested);
        battlefieldEntryService.processFaceDownCreatureETBTriggers(
                gameData, entry.getControllerId(), manifestedCard);

        gameData.expireFloatingEffectsForUnattachedSource(aura.getId());
        aura.setAttachedTo(manifested.getId());
        aura.setTimestamp(gameData.nextTimestamp());
        triggerCollectionService.checkAuraAttachedTriggers(gameData, aura.getCard(), manifested.getId());

        gameLogService.append(gameData, GameLog.builder()
                .card(aura.getCard())
                .text(" becomes an Aura attached to a face-down creature.")
                .build());
        log.info("Game {} - {} becomes an Aura attached to a manifested card",
                gameData.id, aura.getCard().getName());
    }

    private Permanent findSource(GameData gameData, StackEntry entry) {
        if (entry.getSourcePermanentId() != null) {
            return gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        }
        UUID cardId = entry.getCard().getId();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getId().equals(cardId)) {
                    return permanent;
                }
            }
        }
        return null;
    }

    private void becomeAura(Permanent source) {
        if (source.getCard().isAura()) {
            return;
        }
        Card copy = source.getCard().createRuntimeCopy();
        List<CardSubtype> subtypes = new ArrayList<>(copy.getSubtypes());
        if (!subtypes.contains(CardSubtype.AURA)) {
            subtypes.add(CardSubtype.AURA);
        }
        copy.setSubtypes(subtypes);
        if (copy.getTargetFilter() == null) {
            copy.target(TargetFilters.creature());
        }
        copy.freeze();
        source.setCard(copy);
    }
}
