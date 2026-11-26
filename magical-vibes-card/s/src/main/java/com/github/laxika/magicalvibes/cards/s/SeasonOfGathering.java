package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "192")
public class SeasonOfGathering extends Card {

    public SeasonOfGathering() {
        addSpellEffect(ChooseOneEffect.budgetedModes(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Put a +1/+1 counter on a creature you control. It gains vigilance and trample until end of turn.",
                        List.of(
                                ChoosePermanentsEffect.exactly(1, Subjects.creaturesYouControl(), "a creature you control"),
                                new PutCountersEffect(Subjects.thisWay(StepKind.CHOOSE),
                                        CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1)),
                                new GrantKeywordEffect(Keyword.VIGILANCE, Subjects.thisWay(StepKind.CHOOSE),
                                        Duration.UNTIL_END_OF_TURN),
                                new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.thisWay(StepKind.CHOOSE),
                                        Duration.UNTIL_END_OF_TURN)
                        )),
                new ChooseOneEffect.ChooseOneOption(
                        "Choose artifact or enchantment. Destroy all permanents of the chosen type.",
                        new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption("Destroy all artifacts",
                                        DestroyTargetPermanentEffect.all(new PermanentIsArtifactPredicate())),
                                new ChooseOneEffect.ChooseOneOption("Destroy all enchantments",
                                        DestroyTargetPermanentEffect.all(new PermanentIsEnchantmentPredicate()))
                        ))),
                new ChooseOneEffect.ChooseOneOption(
                        "Draw cards equal to the greatest power among creatures you control.",
                        new DrawCardEffect(new GreatestPowerAmongControlled()))
        ), List.of(1, 2, 3), 5));
    }
}
