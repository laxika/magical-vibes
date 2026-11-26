package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.ChooseOneOption;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawUpToNCardsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawUpToRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "48")
public class FatalLore extends Card {

    public FatalLore() {
        addSpellEffect(ChooseOneEffect.anOpponentChooses(List.of(
                new ChooseOneOption("You draw three cards", new DrawCardEffect(3)),
                new ChooseOneOption("You destroy up to two target creatures that player controls; that player draws up to three cards",
                        List.of(DestroyTargetPermanentEffect.eachTarget(true),
                                new DrawUpToNCardsEffect(3, DrawUpToRecipient.MODE_CHOOSER)),
                        TargetFilters.creature(), null, 0, 2, false, null))));
    }
}
