package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

/** Smelt // Herd // Saw, a three-face split spell with one mode for each face. */
@CardRegistration(set = "MB1", collectorNumber = "100")
public class SmeltHerdSaw extends Card {

    public SmeltHerdSaw() {
        CreateTokenEffect elk = new CreateTokenEffect(3, "Elk", 2, 2, CardColor.GREEN,
                List.of(CardSubtype.ELK), Set.of(), Set.of());
        CreateTokenEffect half = new CreateTokenEffect(2, "Half", 1, 2, CardColor.RED,
                List.of(CardSubtype.HALF), Set.of(), Set.of());

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Smelt — Destroy target artifact",
                        new DestroyTargetPermanentEffect(), TargetFilters.artifact())
                        .withManaCost("{R}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Herd — Create three 2/2 green Elk creature tokens",
                        elk)
                        .withManaCost("{5}{G}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Saw — Destroy target creature. Its controller creates two 1/2 red Half creature tokens",
                        new DestroyTargetPermanentEffect(false, half, 2, false), TargetFilters.creature())
                        .withManaCost("{1}{B}")
        )));
    }
}
