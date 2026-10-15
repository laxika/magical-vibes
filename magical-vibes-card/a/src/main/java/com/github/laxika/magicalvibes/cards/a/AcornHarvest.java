package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.LifeCastingCost;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TOR", collectorNumber = "118")
public class AcornHarvest extends Card {

    public AcornHarvest() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(2, "Squirrel", 1, 1,
                CardColor.GREEN, List.of(CardSubtype.SQUIRREL), Set.of(), Set.of()));
        addCastingOption(new FlashbackCast(List.of(
                new ManaCastingCost("{1}{G}"),
                new LifeCastingCost(3))));
    }
}
