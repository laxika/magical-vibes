package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AbruptDecay;
import com.github.laxika.magicalvibes.cards.b.Blightning;
import com.github.laxika.magicalvibes.cards.b.BloodbraidElf;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.LilianaOfTheVeil;
import com.github.laxika.magicalvibes.cards.t.Tarmogoyf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Retrace;
import com.github.laxika.magicalvibes.model.effect.CreateRandomCardCopyAndMayCastEffect;

import java.util.List;
import java.util.function.Supplier;

@CardRegistration(set = "MB2", collectorNumber = "357")
@CardRegistration(set = "MB2", collectorNumber = "596")
public class JundEmOut extends Card {

    private static final List<Supplier<? extends Card>> JUND_CARDS = List.of(
            AbruptDecay::new,
            Blightning::new,
            BloodbraidElf::new,
            LightningBolt::new,
            LilianaOfTheVeil::new,
            Tarmogoyf::new);

    public JundEmOut() {
        addEffect(EffectSlot.SPELL, new CreateRandomCardCopyAndMayCastEffect(JUND_CARDS));
        addCastingOption(new Retrace());
    }
}
