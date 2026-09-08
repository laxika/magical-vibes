package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesUntilEndOfTurnEffect;

import java.util.List;
import java.util.Set;

public class ArlinnTheMoonsFury extends Card {

    public ArlinnTheMoonsFury() {
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new AwardManaEffect(ManaColor.RED), new AwardManaEffect(ManaColor.GREEN)),
                "+2: Add {R}{G}."
        ));

        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(
                        new AnimatePermanentsEffect(5, 5, List.of(CardSubtype.WEREWOLF),
                                Set.of(Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE, Keyword.HASTE)),
                        new SetCardTypesUntilEndOfTurnEffect(Set.of(CardType.CREATURE), GrantScope.SELF)
                ),
                "0: Until end of turn, Arlinn, the Moon's Fury becomes a 5/5 Werewolf creature "
                        + "with trample, indestructible, and haste."
        ));
    }
}
