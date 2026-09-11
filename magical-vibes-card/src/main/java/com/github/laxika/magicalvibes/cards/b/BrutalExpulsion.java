package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetSpellOrCreatureToHandEffect;

import java.util.List;

@CardRegistration(set = "BFZ", collectorNumber = "200")
public class BrutalExpulsion extends Card {

    public BrutalExpulsion() {
        setAllowSharedTargets(true);
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Return target spell or creature to its owner's hand",
                        new ReturnTargetSpellOrCreatureToHandEffect()),
                new ChooseOneEffect.ChooseOneOption(
                        "Brutal Expulsion deals 2 damage to target creature or planeswalker. If that creature or planeswalker would die this turn, exile it instead",
                        new DealDamageToTargetCreatureOrPlaneswalkerEffect(new Fixed(2), true))
        )));
    }
}
