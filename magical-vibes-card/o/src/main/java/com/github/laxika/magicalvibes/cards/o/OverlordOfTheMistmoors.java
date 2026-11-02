package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCardTypeFromAttachedPermanentEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "23")
public class OverlordOfTheMistmoors extends Card {

    public OverlordOfTheMistmoors() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{W}{W}"))));

        CreateTokenEffect insect = new CreateTokenEffect(
                2, "Insect", 2, 1, CardColor.WHITE, List.of(CardSubtype.INSECT),
                Set.of(Keyword.FLYING), Set.of());
        addTrigger(Triggers.enters(ObjectRelation.THIS), insect);
        addEffect(EffectSlot.ON_ATTACK, insect);

        addStaticEffect(new ConditionalEffect(
                new CastForAlternateCost(),
                new EnterWithCountersEffect(CounterType.TIME, new Fixed(4))));
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new ConditionalEffect(
                new AllOf(List.of(
                        new CastForAlternateCost(),
                        new SourceCounterThreshold(1, CounterType.TIME))),
                new RemoveCounterFromSourceEffect(CounterType.TIME, 1)));
        addStaticEffect(new ConditionalEffect(
                new AllOf(List.of(
                        new CastForAlternateCost(),
                        new SourceCounterThreshold(1, CounterType.TIME))),
                new RemoveCardTypeFromAttachedPermanentEffect(CardType.CREATURE, GrantScope.SELF)));
    }
}
