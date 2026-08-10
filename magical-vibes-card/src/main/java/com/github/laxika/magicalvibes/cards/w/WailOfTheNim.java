package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateAllOwnCreaturesEffect;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "81")
public class WailOfTheNim extends Card {

    public WailOfTheNim() {
        addEffect(EffectSlot.SPELL, new EscalateManaCost("{B}"));
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Regenerate each creature you control",
                        new RegenerateAllOwnCreaturesEffect()),
                new ChooseOneEffect.ChooseOneOption(
                        "Wail of the Nim deals 1 damage to each creature and each player",
                        new MassDamageEffect(1, true))
        )));
    }
}
