package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSP", collectorNumber = "249")
public class BrassGnat extends Card {

    public BrassGnat() {
        addStaticEffect(DoesntUntapEffect.self());
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayPayManaEffect(
                "{1}",
                new UntapPermanentsEffect(TapUntapScope.SELF),
                "Pay {1} to untap Brass Gnat?"));
    }
}
