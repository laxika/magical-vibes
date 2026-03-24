package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "XLN", collectorNumber = "227")
public class RegisaurAlpha extends Card {

    public RegisaurAlpha() {
        // Other Dinosaurs you control have haste.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.HASTE, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR)));

        // When this creature enters, create a 3/3 green Dinosaur creature token with trample.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                "Dinosaur", 3, 3, CardColor.GREEN, List.of(CardSubtype.DINOSAUR),
                Set.of(Keyword.TRAMPLE), Set.of()));
    }
}
