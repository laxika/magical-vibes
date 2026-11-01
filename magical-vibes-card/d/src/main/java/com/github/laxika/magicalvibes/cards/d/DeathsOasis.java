package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.DyingPermanentManaValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "182")
public class DeathsOasis extends Card {

    public DeathsOasis() {
        CardEffect deathEffect = nontokenCreatureDeathEffect();
        addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.YOU_CONTROL), deathEffect);

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new SacrificeSelfCost(), new GainLifeEffect(
                        new GreatestManaValueAmongControlled(new PermanentIsCreaturePredicate()))),
                "{1}, Sacrifice this enchantment: You gain life equal to the greatest mana value among creatures you control."
        ));
    }

    private static CardEffect nontokenCreatureDeathEffect() {
        return SequenceEffect.of(
                new MillEffect(2, MillRecipient.CONTROLLER),
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .mandatory(true)
                        .dynamicMaxManaValue(new Sum(new DyingPermanentManaValue(), new Fixed(-1)))
                        .build());
    }
}
