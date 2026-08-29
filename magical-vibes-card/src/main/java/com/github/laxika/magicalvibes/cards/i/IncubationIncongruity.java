package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RNA", collectorNumber = "226")
public class IncubationIncongruity extends Card {

    public IncubationIncongruity() {
        CardEffect incubation = LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                5, new CardTypePredicate(CardType.CREATURE));
        TargetFilter creature = TargetFilters.creature();
        CardEffect incongruity = new ExileTargetPermanentEffect(new CreateTokenEffect(
                "Frog Lizard", 3, 3, CardColor.GREEN,
                List.of(CardSubtype.FROG, CardSubtype.LIZARD), Set.of(), Set.of()));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Incubation — Look at the top five cards of your library. You may reveal a creature card from among them and put it into your hand. Put the rest on the bottom of your library in a random order",
                        incubation
                ).withManaCost("{G/U}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Incongruity — Exile target creature. That creature's controller creates a 3/3 green Frog Lizard creature token",
                        incongruity,
                        creature
                ).withManaCost("{1}{G}{U}")
        )));
    }
}
