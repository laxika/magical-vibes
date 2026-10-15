package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalLifeGainEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SOC", collectorNumber = "42")
@CardRegistration(set = "SOC", collectorNumber = "90")
public class PestRescuer extends Card {

    private static final PermanentAllOfPredicate PEST_CREATURE_TOKEN = new PermanentAllOfPredicate(List.of(
            new PermanentIsTokenPredicate(),
            new PermanentIsCreaturePredicate(),
            new PermanentHasSubtypePredicate(CardSubtype.PEST)));

    public PestRescuer() {
        // At the beginning of each upkeep, if you don't control a Pest creature token, create a 1/1
        // black and green Pest creature token with "When this token dies, you gain 1 life."
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new ConditionalEffect(
                new ControlsPermanentCountAtMost(0, PEST_CREATURE_TOKEN), pestToken()));

        // If you would gain life, you gain that much life plus 1 instead.
        addEffect(EffectSlot.STATIC, new AdditionalLifeGainEffect(1));
    }

    private static CreateTokenEffect pestToken() {
        return new CreateTokenEffect(
                CardType.CREATURE, 1, "Pest", 1, 1,
                CardColor.BLACK, Set.of(CardColor.BLACK, CardColor.GREEN),
                List.of(CardSubtype.PEST), Set.of(), Set.of(),
                false, false,
                Map.of(EffectSlot.ON_DEATH, new GainLifeEffect(1)),
                List.of(), false, false, false, 0, Set.of());
    }
}
