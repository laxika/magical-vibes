package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.SeekFromTopOfLibraryAndPerpetuallyReduceSoughtCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YNEO", collectorNumber = "25")
public class ChroniclerOfWorship extends Card {

    public ChroniclerOfWorship() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new SeekFromTopOfLibraryAndPerpetuallyReduceSoughtCardEffect(
                        7, new CardSubtypePredicate(CardSubtype.SHRINE)));
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
