package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RNA", collectorNumber = "149")
@CardRegistration(set = "DSC", collectorNumber = "205")
@CardRegistration(set = "MKC", collectorNumber = "196")
@CardRegistration(set = "C20", collectorNumber = "196")
public class WildernessReclamation extends Card {

    public WildernessReclamation() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsLandPredicate()));
    }
}
