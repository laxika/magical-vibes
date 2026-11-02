package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CopyImprintedCardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ExileFromHandToImprintEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "188")
@CardRegistration(set = "V10", collectorNumber = "3")
@CardRegistration(set = "EMA", collectorNumber = "223")
@CardRegistration(set = "DDJ", collectorNumber = "16")
@CardRegistration(set = "SLC", collectorNumber = "9")
@CardRegistration(set = "SLC", collectorNumber = "36")
@CardRegistration(set = "2XM", collectorNumber = "264")
@CardRegistration(set = "MB2", collectorNumber = "96")
public class IsochronScepter extends Card {

    public IsochronScepter() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(new ExileFromHandToImprintEffect(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardMaxManaValuePredicate(2))),
                        "an instant card with mana value 2 or less"),
                        "You may exile an instant card with mana value 2 or less from your hand."));
        addActivatedAbility(new ActivatedAbility(true, "{2}",
                List.of(new CopyImprintedCardAndMayCastCopyEffect(false)),
                "{2}, {T}: You may copy the exiled card. If you do, you may cast the copy without paying its mana cost."));
    }
}
