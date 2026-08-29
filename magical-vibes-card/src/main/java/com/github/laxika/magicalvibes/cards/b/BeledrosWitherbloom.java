package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "163")
public class BeledrosWitherbloom extends Card {

    public BeledrosWitherbloom() {
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, pestToken());

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new PayLifeCost(10),
                        new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsLandPredicate())
                ),
                "Pay 10 life: Untap all lands you control. Activate only once each turn.",
                1
        ));
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
