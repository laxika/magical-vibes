package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.ReturnCardExiledWithSourceToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

public class TombOfTheDuskRose extends Card {

    public TombOfTheDuskRose() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{W}{B}",
                List.of(new ReturnCardExiledWithSourceToBattlefieldEffect(
                        new CardTypePredicate(CardType.CREATURE), false, null)),
                "{2}{W}{B}, {T}: Put a creature card exiled with this permanent onto the battlefield under your control."
        ));
    }
}
