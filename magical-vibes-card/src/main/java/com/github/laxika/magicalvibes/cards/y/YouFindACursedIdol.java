package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "213")
public class YouFindACursedIdol extends Card {

    public YouFindACursedIdol() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Smash It — Destroy target artifact",
                        new DestroyTargetPermanentEffect(),
                        TargetFilters.artifact()),
                new ChooseOneEffect.ChooseOneOption(
                        "Lift the Curse — Destroy target enchantment",
                        new DestroyTargetPermanentEffect(),
                        TargetFilters.enchantment()),
                new ChooseOneEffect.ChooseOneOption(
                        "Steal Its Eyes — Create a Treasure token and venture into the dungeon",
                        List.of(CreateTokenEffect.ofTreasureToken(1), new VentureIntoDungeonEffect()))
        )));
    }
}
