package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutLandFromHandThenOpponentsDrawEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C16", collectorNumber = "36")
public class KynaiosAndTiroOfMeletis extends Card {

    public KynaiosAndTiroOfMeletis() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                SequenceEffect.of(
                        new DrawCardEffect(1),
                        new EachPlayerMayPutLandFromHandThenOpponentsDrawEffect()));
    }
}
