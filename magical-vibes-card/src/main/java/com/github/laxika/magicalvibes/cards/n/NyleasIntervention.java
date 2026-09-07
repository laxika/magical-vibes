package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "188")
public class NyleasIntervention extends Card {

    public NyleasIntervention() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Search your library for up to X land cards, reveal them, put them into your hand, then shuffle",
                        new SearchLibraryEffect(new XValue(), new CardTypePredicate(CardType.LAND),
                                LibrarySearchDestination.HAND)),
                new ChooseOneEffect.ChooseOneOption(
                        "Nylea's Intervention deals twice X damage to each creature with flying",
                        new MassDamageEffect(new Scaled(new XValue(), 2), false, false,
                                new PermanentHasKeywordPredicate(Keyword.FLYING))
                )
        )));
    }
}
