package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "177")
public class MercilessEviction extends Card {

    public MercilessEviction() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Exile all artifacts",
                        new ExileAllPermanentsEffect(new PermanentIsArtifactPredicate())),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile all creatures",
                        new ExileAllPermanentsEffect(new PermanentIsCreaturePredicate())),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile all enchantments",
                        new ExileAllPermanentsEffect(new PermanentIsEnchantmentPredicate())),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile all planeswalkers",
                        new ExileAllPermanentsEffect(new PermanentIsPlaneswalkerPredicate()))
        )));
    }
}
