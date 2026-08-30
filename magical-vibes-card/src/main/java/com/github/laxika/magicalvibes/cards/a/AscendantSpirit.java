package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureTypeWithBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.condition.SourceHasSubtype;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "43")
public class AscendantSpirit extends Card {

    public AscendantSpirit() {
        addActivatedAbility(new ActivatedAbility(false, "{S}{S}",
                List.of(new BecomeCreatureTypeWithBasePowerToughnessEffect(2, 3, CardSubtype.WARRIOR)),
                "{S}{S}: This creature becomes a Spirit Warrior with base power and toughness 2/3."));

        addActivatedAbility(new ActivatedAbility(false, "{S}{S}{S}", List.of(
                new ConditionalEffect(new SourceHasSubtype(CardSubtype.WARRIOR), SequenceEffect.of(
                        new PutCountersOnSelfEffect(CounterType.FLYING),
                        new BecomeCreatureTypeWithBasePowerToughnessEffect(4, 4, CardSubtype.ANGEL)
                ))),
                "{S}{S}{S}: If this creature is a Warrior, put a flying counter on it and it becomes a Spirit Warrior Angel with base power and toughness 4/4."));

        addActivatedAbility(new ActivatedAbility(false, "{S}{S}{S}{S}", List.of(
                new ConditionalEffect(new SourceHasSubtype(CardSubtype.ANGEL), SequenceEffect.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                        new GrantEffectToTargetEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                                new DrawCardEffect(), com.github.laxika.magicalvibes.model.effect.EffectDuration.PERMANENT,
                                false, GrantScope.SELF)
                ))),
                "{S}{S}{S}{S}: If this creature is an Angel, put two +1/+1 counters on it and it gains \"Whenever this creature deals combat damage to a player, draw a card.\""));
    }
}
