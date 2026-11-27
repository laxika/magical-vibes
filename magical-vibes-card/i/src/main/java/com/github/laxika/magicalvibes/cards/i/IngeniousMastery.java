package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentCreatesTokenEffect;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "44")
public class IngeniousMastery extends Card {

    public IngeniousMastery() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{U}"))));
        addSpellEffect(new ConditionalEffect(
                new CastForAlternateCost(),
                // "An opponent creates two Treasure tokens and they scry 2": the caster chooses that opponent as the
                // spell resolves (the multiplayer ruling), after the draw.
                SequenceEffect.of(
                        new DrawCardEffect(3),
                        ChoosePlayerEffect.opponent(),
                        TargetOpponentCreatesTokenEffect.createdBy(Subjects.playersThisWay(StepKind.CHOOSE),
                                CreateTokenEffect.ofTreasureToken(2)),
                        new ScryEffect(2, Subjects.playersThisWay(StepKind.CHOOSE)))));
        addSpellEffect(new ConditionalEffect(
                new NotCondition(new CastForAlternateCost()), new DrawCardEffect(new XValue())));
    }
}
