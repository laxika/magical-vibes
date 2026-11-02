package com.github.laxika.magicalvibes.cards.t;



import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AKH", collectorNumber = "153")

public class TrueheartTwins extends Card {

    public TrueheartTwins() {
        addTrigger(Triggers.attacks(), new MayEffect(
                new SkipNextUntapEffect(TapUntapScope.SELF, null, 1, false, false, true),
                "Exert Trueheart Twins as it attacks?"));

        addTrigger(Triggers.youExertCreature(), new BoostAllOwnCreaturesEffect(1, 0));
    }
}
