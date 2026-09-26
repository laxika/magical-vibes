package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnCreatedPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LTC", collectorNumber = "36")
@CardRegistration(set = "LTC", collectorNumber = "119")
public class AssembleTheEntmoot extends Card {

    public AssembleTheEntmoot() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.REACH, GrantScope.OWN_CREATURES));

        LifeGainedThisTurn lifeGained = new LifeGainedThisTurn(CountScope.CONTROLLER);
        CreateTokenEffect treefolk = new CreateTokenEffect(
                "Treefolk", lifeGained, lifeGained, CardColor.GREEN,
                List.of(CardSubtype.TREEFOLK), Set.of(), Set.of())
                .withAmount(3)
                .withTapped(true);
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        treefolk,
                        new PutCountersOnCreatedPermanentsEffect(CounterType.REACH, new Fixed(1))
                ),
                "Sacrifice this enchantment: Create three tapped X/X green Treefolk creature tokens, where X is the amount of life you gained this turn. Put a reach counter on each of them."
        ));
    }
}
