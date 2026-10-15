package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "200")
public class KuraTheBoundlessSky extends Card {

    public KuraTheBoundlessSky() {
        PermanentCount landsYouControl = new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER);

        addEffect(EffectSlot.ON_DEATH, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Search your library for up to three land cards, reveal them, and put them into your hand",
                        new SearchLibraryEffect(new Fixed(3), new CardTypePredicate(CardType.LAND),
                                LibrarySearchDestination.HAND)),
                new ChooseOneEffect.ChooseOneOption(
                        "Create an X/X green Spirit creature token, where X is the number of lands you control",
                        new CreateTokenEffect("Spirit", landsYouControl, landsYouControl, CardColor.GREEN,
                                List.of(CardSubtype.SPIRIT), Set.of(), Set.of()))
        )));
    }
}
