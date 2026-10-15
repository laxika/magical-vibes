package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.PlayerAttacksOneOfYourOpponents;
import com.github.laxika.magicalvibes.model.effect.AddTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "NCC", collectorNumber = "73")
@CardRegistration(set = "NCC", collectorNumber = "173")
public class JoleneThePlunderQueen extends Card {

    public JoleneThePlunderQueen() {
        addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS,
                new ConditionalEffect(
                        new PlayerAttacksOneOfYourOpponents(),
                        new CreateTokenForTriggeringPlayerEffect(CreateTokenEffect.ofTreasureToken(1))));
        addEffect(EffectSlot.STATIC, new AddTokenCreationEffect(1, CardSubtype.TREASURE));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeMultiplePermanentsCost(
                                5, new PermanentHasSubtypePredicate(CardSubtype.TREASURE)),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 5)
                ),
                "Sacrifice five Treasures: Put five +1/+1 counters on Jolene."
        ));
    }
}
