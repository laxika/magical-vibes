package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.condition.SpellCreatureManaSpentAtLeast;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "229")
public class IngaAndEsika extends Card {

    public IngaAndEsika() {
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.ALL_OWN_CREATURES));
        addEffect(EffectSlot.STATIC,
                new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                true,
                                null,
                                List.of(new AwardAnyColorManaEffect(1, ManaSpendRestriction.CREATURE_SPELL_ONLY)),
                                "{T}: Add one mana of any color. Spend this mana only to cast a creature spell."),
                        GrantScope.ALL_OWN_CREATURES));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                SpellCastTriggerEffect.withIntervening(
                        new CardTypePredicate(CardType.CREATURE),
                        List.of(new DrawCardEffect()),
                        new SpellCreatureManaSpentAtLeast(3)));
    }
}
