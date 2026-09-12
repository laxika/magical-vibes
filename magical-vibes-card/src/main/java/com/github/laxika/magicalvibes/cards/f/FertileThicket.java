package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "DDR", collectorNumber = "27")
public class FertileThicket extends Card {

    public FertileThicket() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new LookAtTopCardsEffect(
                        new Fixed(5), new Fixed(1), CardPredicateUtils.basicLand(),
                        LookDestination.BOTTOM_OF_LIBRARY, false,
                        LibrarySearchDestination.TOP_OF_LIBRARY, true),
                "Look at the top five cards of your library?"));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
