package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DGM", collectorNumber = "123")
public class BeckCall extends Card {

    public BeckCall() {
        CardEffect beck = new RegisterGlobalTriggeredAbilityEffect(new TriggeredAbility(
                Triggers.enters(new PermanentIsCreaturePredicate(), ObjectRelation.ANY),
                new MayEffect(new DrawCardEffect(), "Draw a card?")));
        CardEffect call = new CreateTokenEffect(
                4, "Bird", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of());

        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Beck — Whenever a creature enters this turn, you may draw a card.", beck
                ).withManaCost("{G}{U}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Call — Create four 1/1 white Bird creature tokens with flying.", call
                ).withManaCost("{4}{W}{U}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Fuse — Beck and then Call",
                        List.of(beck, call)
                ).withManaCost("{4}{G}{W}{U}{U}")
        )));
    }
}
