package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOI", collectorNumber = "63")
@CardRegistration(set = "SIR", collectorNumber = "71")
@CardRegistration(set = "MIC", collectorNumber = "100")
@CardRegistration(set = "DRC", collectorNumber = "77")
public class ForgottenCreation extends Card {

    public ForgottenCreation() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                new DiscardOwnHandThenDrawThatManyEffect(),
                "Discard your hand and draw that many cards?"));
    }
}
