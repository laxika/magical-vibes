package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "224")
public class RagostDeftGastronaut extends Card {

    public RagostDeftGastronaut() {
        addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(
                CardSubtype.FOOD, GrantScope.OWN_PERMANENTS, false, new PermanentIsArtifactPredicate()));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                foodAbility(), GrantScope.OWN_PERMANENTS, new PermanentIsArtifactPredicate()));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.FOOD), "a Food", false),
                        new DealDamageToPlayersEffect(3, DamageRecipient.EACH_OPPONENT)),
                "{1}, {T}, Sacrifice a Food: Ragost deals 3 damage to each opponent."));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new GainedLifeThisTurn(), new UntapPermanentsEffect(TapUntapScope.SELF)));
    }

    private static ActivatedAbility foodAbility() {
        return new ActivatedAbility(
                true,
                "{2}",
                List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                "{2}, {T}, Sacrifice this artifact: You gain 3 life.");
    }
}
