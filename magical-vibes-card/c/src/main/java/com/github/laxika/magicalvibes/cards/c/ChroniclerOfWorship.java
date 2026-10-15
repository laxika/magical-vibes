package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.SeekFromTopOfLibraryAndPerpetuallyReduceSoughtCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "YNEO", collectorNumber = "25")
public class ChroniclerOfWorship extends Card {

    public ChroniclerOfWorship() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SeekFromTopOfLibraryAndPerpetuallyReduceSoughtCardEffect(
                        7, new CardSubtypePredicate(CardSubtype.SHRINE)));
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
