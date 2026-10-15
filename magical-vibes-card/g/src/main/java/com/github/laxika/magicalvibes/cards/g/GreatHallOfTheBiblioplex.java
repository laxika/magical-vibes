package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsCreature;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOS", collectorNumber = "257")
public class GreatHallOfTheBiblioplex extends Card {

    private static final CardAnyOfPredicate INSTANT_OR_SORCERY = new CardAnyOfPredicate(List.of(
            new CardTypePredicate(CardType.INSTANT),
            new CardTypePredicate(CardType.SORCERY)));

    public GreatHallOfTheBiblioplex() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PayLifeCost(1),
                        new AwardAnyColorManaEffect(1, ManaSpendRestriction.INSTANT_SORCERY_ONLY)),
                "{T}, Pay 1 life: Add one mana of any color. Spend this mana only to cast an instant or sorcery spell."
        ));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(new SourceIsCreature(),
                new GrantTriggeredAbilityEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                        new SpellCastTriggerEffect(INSTANT_OR_SORCERY, List.of(new BoostSelfEffect(1, 0))),
                        GrantScope.SELF)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(new ConditionalEffect(
                        new NotCondition(new SourceIsCreature()),
                        new AnimatePermanentsEffect(2, 4, List.of(CardSubtype.WIZARD), Set.of(), null,
                                Set.of(), GrantScope.SELF, EffectDuration.PERMANENT))),
                "{5}: If this land isn't a creature, it becomes a 2/4 Wizard creature with \"Whenever you cast an instant or sorcery spell, this creature gets +1/+0 until end of turn.\" It's still a land."
        ));
    }
}
