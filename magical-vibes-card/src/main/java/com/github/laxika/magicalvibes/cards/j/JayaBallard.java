package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUpToThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.model.effect.JayaBallardEmblemEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "132")
public class JayaBallard extends Card {

    public JayaBallard() {
        // +1: Add {R}{R}{R}. Spend this mana only to cast instant or sorcery spells.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new AwardRestrictedManaEffect(ManaColor.RED, 3, Set.of(CardType.INSTANT, CardType.SORCERY))),
                "+1: Add {R}{R}{R}. Spend this mana only to cast instant or sorcery spells."
        ));

        // +1: Discard up to three cards, then draw that many cards.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new DiscardUpToThenDrawThatManyEffect(3)),
                "+1: Discard up to three cards, then draw that many cards."
        ));

        // −8: You get an emblem with "You may cast instant and sorcery spells from your graveyard.
        // If a spell cast this way would be put into your graveyard, exile it instead."
        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new JayaBallardEmblemEffect()),
                "\u22128: You get an emblem with \"You may cast instant and sorcery spells from your graveyard. If a spell cast this way would be put into your graveyard, exile it instead.\""
        ));
    }
}
