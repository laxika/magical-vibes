package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLC", collectorNumber = "1")
@CardRegistration(set = "BLC", collectorNumber = "101")
public class BelloBardOfTheBrambles extends Card {

    public BelloBardOfTheBrambles() {
        PermanentPredicate artifact = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT)),
                new PermanentMinManaValuePredicate(4)
        ));
        PermanentPredicate enchantment = new PermanentAllOfPredicate(List.of(
                new PermanentIsEnchantmentPredicate(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.AURA)),
                new PermanentMinManaValuePredicate(4)
        ));
        PermanentPredicate eligiblePermanent = new PermanentAnyOfPredicate(List.of(artifact, enchantment));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new AnimatePermanentsEffect(
                        new Fixed(4), new Fixed(4), List.of(CardSubtype.ELEMENTAL),
                        Set.of(Keyword.INDESTRUCTIBLE, Keyword.HASTE), null,
                        Set.of(CardType.CREATURE), GrantScope.OWN_PERMANENTS,
                        EffectDuration.CONTINUOUS, eligiblePermanent)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                        new DrawCardEffect(), GrantScope.OWN_PERMANENTS, eligiblePermanent)));
    }
}
