package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MH1", collectorNumber = "117")
public class AlpineGuide extends Card {

    public AlpineGuide() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new SearchLibraryEffect(
                        new CardSubtypePredicate(CardSubtype.MOUNTAIN),
                        LibrarySearchDestination.BATTLEFIELD_TAPPED),
                        "Search your library for a Mountain card?"));
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new SacrificePermanentsEffect(1,
                        new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN),
                        SacrificeRecipient.CONTROLLER));
    }
}
