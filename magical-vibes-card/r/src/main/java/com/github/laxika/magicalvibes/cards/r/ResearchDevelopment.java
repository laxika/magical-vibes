package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnyOpponentMayDrawOrCreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleUpToNCardsFromOutsideGameIntoLibraryEffect;

import java.util.List;
import java.util.Set;

/** Research // Development, a split spell with one mode for each half. */
@CardRegistration(set = "DIS", collectorNumber = "155")
public class ResearchDevelopment extends Card {

    public ResearchDevelopment() {
        CreateTokenEffect elemental = new CreateTokenEffect(
                1, "Elemental", 3, 1, CardColor.RED, List.of(CardSubtype.ELEMENTAL), Set.of(), Set.of());
        List<CardEffect> development = List.of(
                new AnyOpponentMayDrawOrCreateTokenEffect(elemental),
                new AnyOpponentMayDrawOrCreateTokenEffect(elemental),
                new AnyOpponentMayDrawOrCreateTokenEffect(elemental));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Research - Shuffle up to four cards you own from outside the game into your library",
                        new ShuffleUpToNCardsFromOutsideGameIntoLibraryEffect(4)
                ).withManaCost("{G}{U}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Development - Create a 3/1 red Elemental creature token unless an opponent has you draw a card, repeated three times",
                        development
                ).withManaCost("{3}{U}{R}")
        )));
    }
}
