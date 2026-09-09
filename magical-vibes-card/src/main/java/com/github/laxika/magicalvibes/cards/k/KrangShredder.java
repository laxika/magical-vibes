package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.PermanentLeftBattlefieldUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandOfEachOpponentWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastCardExiledWithSourceEffect;

@CardRegistration(set = "TMT", collectorNumber = "153")
@CardRegistration(set = "TMT", collectorNumber = "201")
@CardRegistration(set = "TMT", collectorNumber = "245")
public class KrangShredder extends Card {

    public KrangShredder() {
        ExileTopUntilNonlandOfEachOpponentWithSourceEffect exileEffect =
                new ExileTopUntilNonlandOfEachOpponentWithSourceEffect();
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, exileEffect);
        addEffect(EffectSlot.ON_ATTACK, exileEffect);
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(new PermanentLeftBattlefieldUnderYourControlThisTurn(),
                        new MayCastCardExiledWithSourceEffect()));
    }
}
