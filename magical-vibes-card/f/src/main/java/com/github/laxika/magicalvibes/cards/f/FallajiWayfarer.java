package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;

@CardRegistration(set = "DMC", collectorNumber = "11")
@CardRegistration(set = "DMC", collectorNumber = "87")
public class FallajiWayfarer extends Card {

    public FallajiWayfarer() {
        for (CardColor color : CardColor.values()) {
            addStaticEffect(new GrantColorEffect(color, GrantScope.SELF));
        }
        addStaticEffect(new GrantSpellCastingAbilityToSpellsEffect(
                Keyword.CONVOKE, new CardIsMulticoloredPredicate()));
    }
}
