package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardOnDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnTargetCardOnDeathThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCardOnDeathThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            log.info("Game {} - Target creature no longer on battlefield, delayed return trigger not registered", gameData.id);
            return;
        }

        // "When target creature dies this turn, return that card to the battlefield": about the creature, with this
        // turn as its stated duration (CR 603.7b); "put into your graveyard" (Saffi Eriksdotter) only for a card the
        // ability's controller owns.
        var register = (ReturnTargetCardOnDeathThisTurnEffect) effect;
        PermanentPredicate filter = register.requireControllerGraveyard()
                ? new PermanentOwnedBySourceControllerPredicate() : null;
        PutCardsOntoBattlefieldEffect returnIt = new PutCardsOntoBattlefieldEffect(Subjects.triggeringCards(),
                register.enterTapped());
        if (register.returnUnderController()) {
            returnIt = returnIt.underControlOf(Subjects.you());
        }
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.about(
                entry.getControllerId(), entry.getCard(),
                new TriggeredAbility(Triggers.dies(filter, ObjectRelation.THIS),
                        returnIt),
                target.getId(), entry.getSourcePermanentId(), true));

        log.info("Game {} - Delayed trigger registered: if {} dies this turn, return it to the battlefield",
                gameData.id, target.getCard().getName());
    }
}
