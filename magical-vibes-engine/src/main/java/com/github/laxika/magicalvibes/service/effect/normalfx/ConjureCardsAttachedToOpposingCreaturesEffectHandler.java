package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsAttachedToOpposingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * "For each creature your opponents control, conjure a card named … onto the battlefield attached to that creature":
 * the creatures are found once as the ability resolves (CR 608.2h), and each Aura card enters attached to its creature.
 * An Aura that couldn't legally enchant its creature isn't conjured (CR 303.4i). The cards conjured are one conjure
 * event.
 */
@Component
@RequiredArgsConstructor
public class ConjureCardsAttachedToOpposingCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final AuraAttachmentService auraAttachmentService;
    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureCardsAttachedToOpposingCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureCardsAttachedToOpposingCreaturesEffect conjure = (ConjureCardsAttachedToOpposingCreaturesEffect) effect;
        CardSet set = CardSet.findByCode(conjure.setCode());
        if (set == null) {
            throw new IllegalArgumentException("Unknown card set: " + conjure.setCode());
        }
        UUID controllerId = entry.getControllerId();
        List<Permanent> creatures = subjectResolutionService.permanents(
                gameData, entry, effect, Subjects.creaturesYourOpponentsControl());
        int conjured = 0;
        for (Permanent creature : creatures) {
            if (gameQueryService.findPermanentById(gameData, creature.getId()) == null) {
                continue;
            }
            Card aura = cardCatalog.findByCollectorNumber(set, conjure.collectorNumber()).createCard();
            aura.setConjured(true);
            aura.setOwnerId(controllerId);
            if (!auraAttachmentService.canEnchant(gameData, aura, controllerId, creature)) {
                continue;
            }
            Permanent auraPermanent = new Permanent(aura);
            auraPermanent.setAttachedTo(creature.getId());
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, auraPermanent);
            conjured++;
            if (gameQueryService.findPermanentById(gameData, auraPermanent.getId()) == null) {
                continue;
            }
            entry.getCreatedPermanentIds().add(auraPermanent.getId());
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " conjures " + aura.getName()
                    + " onto the battlefield attached to " + FaceDownVisibility.publicName(gameData, creature) + "."));
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, aura, null, false);
        }
        ActionEvents.conjured(gameData, controllerId, conjured);
    }
}
