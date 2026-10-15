package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "OTC", collectorNumber = "20")
@CardRegistration(set = "OTC", collectorNumber = "56")
public class DiscreetRetreat extends Card {

    private static final CardAnyOfPredicate OUTLAW = new CardAnyOfPredicate(List.of(
            new CardSubtypePredicate(CardSubtype.ASSASSIN),
            new CardSubtypePredicate(CardSubtype.MERCENARY),
            new CardSubtypePredicate(CardSubtype.PIRATE),
            new CardSubtypePredicate(CardSubtype.ROGUE),
            new CardSubtypePredicate(CardSubtype.WARLOCK)));

    public DiscreetRetreat() {
        target(TargetFilters.land())
                .addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                true,
                                null,
                                List.of(new AwardAnyColorManaEffect(
                                        new Fixed(2),
                                        ManaSpendRestriction.SUBTYPE_SPELL_OR_ABILITY,
                                        CardSubtype.OUTLAW,
                                        false,
                                        false)),
                                "{T}: Add two mana of any one color. Spend this mana only to cast outlaw spells or activate abilities of outlaw sources."),
                        GrantScope.ENCHANTED_PERMANENT));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, SpellCastTriggerEffect.nth(
                1,
                OUTLAW,
                List.of(new DrawCardEffect(), new LoseLifeEffect(1))));
    }
}
