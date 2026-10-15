package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsSaddled;
import com.github.laxika.magicalvibes.model.effect.BecomeSaddledUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SaddleCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DFT", collectorNumber = "153")
public class AutarchMammoth extends Card {

    public AutarchMammoth() {
        CreateTokenEffect elephant = new CreateTokenEffect(
                "Elephant", 3, 3, CardColor.GREEN, List.of(CardSubtype.ELEPHANT), Set.of(), Set.of());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, elephant);
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(new SourceIsSaddled(), elephant));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SaddleCost(5), new BecomeSaddledUntilEndOfTurnEffect(GrantScope.SELF)),
                "Saddle 5",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
