package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.PermanentLeftBattlefieldUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandOfEachOpponentWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastCardExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMT", collectorNumber = "153")
@CardRegistration(set = "TMT", collectorNumber = "201")
@CardRegistration(set = "TMT", collectorNumber = "245")
public class KrangShredder extends Card {

    public KrangShredder() {
        ExileTopUntilNonlandOfEachOpponentWithSourceEffect exileEffect =
                new ExileTopUntilNonlandOfEachOpponentWithSourceEffect();
        addTrigger(Triggers.enters(ObjectRelation.THIS), exileEffect);
        addEffect(EffectSlot.ON_ATTACK, exileEffect);
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new ConditionalEffect(new PermanentLeftBattlefieldUnderYourControlThisTurn(),
                        new MayCastCardExiledWithSourceEffect()));
    }
}
