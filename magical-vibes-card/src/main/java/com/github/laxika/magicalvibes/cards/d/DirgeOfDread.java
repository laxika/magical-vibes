package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "138")
public class DirgeOfDread extends Card {

    public DirgeOfDread() {
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.FEAR, GrantScope.ALL_CREATURES));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(
                        new GrantKeywordEffect(Keyword.FEAR, GrantScope.TARGET),
                        new DrawCardEffect(1)),
                "Cycling {1}{B} ({1}{B}, Discard this card: Draw a card.)",
                TargetFilters.creature(),
                null,
                null,
                null,
                List.of(),
                0,
                1));
    }
}
