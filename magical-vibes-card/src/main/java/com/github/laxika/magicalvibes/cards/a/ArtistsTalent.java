package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceClassLevelAtLeast;
import com.github.laxika.magicalvibes.model.effect.AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ClassLevelUpEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "124")
public class ArtistsTalent extends Card {

    public ArtistsTalent() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(new MayEffect(
                        new DiscardAndDrawCardEffect(),
                        "Discard a card to draw a card?"))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new ClassLevelUpEffect(2)),
                "{2}{R}: Level 2. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new NotCondition(new SourceClassLevelAtLeast(2)),
                "This Class is already level 2 or higher."));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new ClassLevelUpEffect(3)),
                "{2}{R}: Level 3. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new AllOf(List.of(
                        new SourceClassLevelAtLeast(2),
                        new NotCondition(new SourceClassLevelAtLeast(3)))),
                "This Class must be level 2."));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceClassLevelAtLeast(2),
                new ReduceCastCostForMatchingSpellsEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        1,
                        CostModificationScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceClassLevelAtLeast(3),
                new AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(2, true)));
    }
}
