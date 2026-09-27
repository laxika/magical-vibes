package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SS3", collectorNumber = "3")
@CardRegistration(set = "TLE", collectorNumber = "165")
@CardRegistration(set = "AA2", collectorNumber = "10")
@CardRegistration(set = "C15", collectorNumber = "26")
@CardRegistration(set = "CMM", collectorNumber = "222")
@CardRegistration(set = "CMM", collectorNumber = "536")
public class FieryConfluence extends Card {

    public FieryConfluence() {
        addEffect(EffectSlot.SPELL, ChooseOneEffect.withRepeatedModes(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Fiery Confluence deals 1 damage to each creature",
                        new MassDamageEffect(1)),
                new ChooseOneEffect.ChooseOneOption(
                        "Fiery Confluence deals 2 damage to each opponent",
                        new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT)),
                ChooseOneEffect.ChooseOneOption.withEffectFactory(
                        "Destroy target artifact",
                        () -> new DestroyTargetPermanentEffect(TargetFilters.artifact().predicate()),
                        TargetFilters.artifact())
        ), 3));
    }
}
