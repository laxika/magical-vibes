package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MarkTargetCreatureExileInsteadOfDieThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "164")
public class Suplex extends Card {

    public Suplex() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Suplex deals 3 damage to target creature",
                        List.of((CardEffect) SequenceEffect.of(
                                new MarkTargetCreatureExileInsteadOfDieThisTurnEffect(),
                                new DealDamageToTargetCreatureEffect(3))),
                        List.of(TargetFilters.creature())),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile target artifact",
                        new ExileTargetPermanentEffect(),
                        TargetFilters.artifact())
        )));
    }
}
