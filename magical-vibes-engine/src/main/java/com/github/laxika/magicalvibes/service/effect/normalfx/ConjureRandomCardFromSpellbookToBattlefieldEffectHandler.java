package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureRandomCardFromSpellbookToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves a random full card from a named spellbook onto the battlefield. */
@Component
@RequiredArgsConstructor
public class ConjureRandomCardFromSpellbookToBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureRandomCardFromSpellbookToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var conjure = (ConjureRandomCardFromSpellbookToBattlefieldEffect) effect;
        List<Card> candidates = new ArrayList<>();
        for (DraftFromSpellbookEffect.SpellbookCard reference : conjure.spellbook()) {
            Card card = cardCatalog.createCardIfImplemented(reference.setCode(), reference.collectorNumber());
            if (card != null) {
                card.setConjured(true);
                candidates.add(card);
            }
        }
        if (candidates.isEmpty()) {
            return;
        }

        Card conjuredCard = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        conjuredCard.setOwnerId(entry.getControllerId());

        Permanent permanent = new Permanent(conjuredCard);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, entry.getControllerId(), permanent);
        triggerCollectionService.checkConjureTriggers(gameData, entry.getControllerId(), 1);
        if (gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of()).stream()
                .anyMatch(candidate -> candidate.getId().equals(permanent.getId()))) {
            entry.getCreatedPermanentIds().add(permanent.getId());
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    " conjures " + conjuredCard.getName() + " onto the battlefield."));
            if (conjuredCard.hasType(CardType.LAND)) {
                battlefieldEntryService.processLandETBEffects(
                        gameData, entry.getControllerId(), conjuredCard);
            } else {
                battlefieldEntryService.handleCreatureEnteredBattlefield(
                        gameData, entry.getControllerId(), conjuredCard, null, false);
            }
        }
        Card perpetualCard = permanent.getCard().createRuntimeCopy();
        perpetualCard.setPower(conjure.basePower());
        perpetualCard.setToughness(conjure.baseToughness());
        permanent.exchangeCard(perpetualCard);
    }
}
