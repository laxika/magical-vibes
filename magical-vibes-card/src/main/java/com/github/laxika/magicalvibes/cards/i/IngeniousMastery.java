package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentCreatesTokenEffect;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "44")
public class IngeniousMastery extends Card {

    public IngeniousMastery() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{U}"))));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new CastForAlternateCost(),
                SequenceEffect.of(
                        new DrawCardEffect(3),
                        new TargetOpponentCreatesTokenEffect(CreateTokenEffect.ofTreasureToken(2)),
                        new ScryEffect(2, LibraryOwner.OPPONENT))));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new NotCondition(new CastForAlternateCost()), new DrawCardEffect(new XValue())));
    }
}
