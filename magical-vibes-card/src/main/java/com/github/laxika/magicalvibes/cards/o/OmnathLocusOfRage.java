package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BFZ", collectorNumber = "217")
public class OmnathLocusOfRage extends Card {

    private static final DealDamageToAnyTargetEffect DEATH_DAMAGE = new DealDamageToAnyTargetEffect(3);

    public OmnathLocusOfRage() {
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, new CreateTokenEffect(
                "Elemental", 5, 5, CardColor.RED, Set.of(CardColor.RED, CardColor.GREEN),
                List.of(CardSubtype.ELEMENTAL)));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.ELEMENTAL), DEATH_DAMAGE));
        addEffect(EffectSlot.ON_DEATH, DEATH_DAMAGE);
    }
}
