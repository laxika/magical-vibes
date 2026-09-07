package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToChosenCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnChosenOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "192")
public class SeasonOfGathering extends Card {

    public SeasonOfGathering() {
        addEffect(EffectSlot.SPELL, ChooseOneEffect.budgetedModes(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Put a +1/+1 counter on a creature you control. It gains vigilance and trample until end of turn.",
                        List.of(
                                new PutCounterOnChosenOwnPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1,
                                        new PermanentIsCreaturePredicate()),
                                new GrantKeywordToChosenCreatureUntilEndOfTurnEffect(Keyword.VIGILANCE, null),
                                new GrantKeywordToChosenCreatureUntilEndOfTurnEffect(Keyword.TRAMPLE, null)
                        )),
                new ChooseOneEffect.ChooseOneOption(
                        "Choose artifact or enchantment. Destroy all permanents of the chosen type.",
                        new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption("Destroy all artifacts",
                                        new DestroyAllPermanentsEffect(new PermanentIsArtifactPredicate())),
                                new ChooseOneEffect.ChooseOneOption("Destroy all enchantments",
                                        new DestroyAllPermanentsEffect(new PermanentIsEnchantmentPredicate()))
                        ))),
                new ChooseOneEffect.ChooseOneOption(
                        "Draw cards equal to the greatest power among creatures you control.",
                        new DrawCardEffect(new GreatestPowerAmongControlled()))
        ), List.of(1, 2, 3), 5));
    }
}
