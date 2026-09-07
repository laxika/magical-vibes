package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOneFromTopCardsFaceDownWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WAR", collectorNumber = "180")
public class VivienChampionOfTheWilds extends Card {

    public VivienChampionOfTheWilds() {
        addEffect(EffectSlot.STATIC,
                new GrantFlashToCardTypeEffect(new CardTypePredicate(CardType.CREATURE)));
        addEffect(EffectSlot.STATIC,
                new AllowCastFromCardsExiledWithSourceEffect(
                        false, new CardTypePredicate(CardType.CREATURE), false, false, 0));

        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new GrantKeywordEffect(
                        Set.of(Keyword.REACH, Keyword.VIGILANCE),
                        GrantScope.TARGET, GrantDuration.UNTIL_YOUR_NEXT_TURN)),
                "+1: Until your next turn, up to one target creature gains reach and vigilance.",
                TargetFilters.creature(), +1, null, null,
                List.of(), 0, 1));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new ExileOneFromTopCardsFaceDownWithSourceEffect(3)),
                "−2: Look at the top three cards of your library. Exile one face down and put the rest "
                        + "on the bottom of your library in any order. For as long as it remains exiled, "
                        + "you may cast it if it's a creature spell."));
    }
}
