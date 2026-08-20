package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "CSP", collectorNumber = "145")
public class DarkDepths extends Card {

    public DarkDepths() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.ICE, new Fixed(10)));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new RemoveCounterFromSourceCost(1, CounterType.ICE)),
                "Remove an ice counter from Dark Depths."
        ));

        CreateTokenEffect maritLage = new CreateTokenEffect(
                CardType.CREATURE,
                1,
                "Marit Lage",
                20,
                20,
                CardColor.BLACK,
                null,
                List.of(CardSubtype.AVATAR),
                Set.of(Keyword.FLYING, Keyword.INDESTRUCTIBLE),
                Set.of(),
                false,
                false,
                Map.of(),
                List.of(),
                false,
                false,
                true,
                0,
                Set.of());

        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                new PermanentNotPredicate(new PermanentHasCountersPredicate(CounterType.ICE)),
                List.of(new SacrificeSelfThenEffect(maritLage)),
                "Dark Depths's state-triggered ability"));
    }
}
