package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseKeptPermanentOfEachTypeThenSacrificeRestEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "176")
@CardRegistration(set = "WAR", collectorNumber = "97")
public class LilianaDreadhordeGeneral extends Card {

    public LilianaDreadhordeGeneral() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new DrawCardEffect(1));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new CreateTokenEffect("Zombie", 2, 2, CardColor.BLACK,
                        List.of(CardSubtype.ZOMBIE), Set.of(), Set.of())),
                "+1: Create a 2/2 black Zombie creature token."
        ));

        addActivatedAbility(new ActivatedAbility(
                -4,
                List.of(new SacrificePermanentsEffect(
                        2,
                        new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate())),
                        SacrificeRecipient.EACH_PLAYER)),
                "−4: Each player sacrifices two creatures of their choice."
        ));

        addActivatedAbility(new ActivatedAbility(
                -9,
                List.of(new ChooseKeptPermanentOfEachTypeThenSacrificeRestEffect(
                        List.of(CardType.ARTIFACT, CardType.BATTLE, CardType.CREATURE,
                                CardType.ENCHANTMENT, CardType.LAND, CardType.PLANESWALKER),
                        true, true, SacrificeRecipient.EACH_OPPONENT)),
                "−9: Each opponent chooses a permanent they control of each permanent type and sacrifices the rest."
        ));
    }
}
