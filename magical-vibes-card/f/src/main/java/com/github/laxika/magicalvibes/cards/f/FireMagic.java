package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TieredManaCost;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "136")
public class FireMagic extends Card {

    public FireMagic() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Fire - {0} - Fire Magic deals 1 damage to each creature",
                        new MassDamageEffect(1)),
                new ChooseOneEffect.ChooseOneOption(
                        "Fira - {2} - Fire Magic deals 2 damage to each creature",
                        new MassDamageEffect(2)),
                new ChooseOneEffect.ChooseOneOption(
                        "Firaga - {5} - Fire Magic deals 3 damage to each creature",
                        new MassDamageEffect(3))
        )));
        addEffect(EffectSlot.SPELL, new TieredManaCost(List.of("", "{2}", "{5}")));
    }
}
