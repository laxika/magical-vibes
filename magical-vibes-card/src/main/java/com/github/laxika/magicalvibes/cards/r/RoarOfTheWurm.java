package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ODY", collectorNumber = "266")
public class RoarOfTheWurm extends Card {

    public RoarOfTheWurm() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Wurm",
                6,
                6,
                CardColor.GREEN,
                List.of(CardSubtype.WURM),
                Set.of(),
                Set.of()));
        addCastingOption(new FlashbackCast("{3}{G}"));
    }
}
