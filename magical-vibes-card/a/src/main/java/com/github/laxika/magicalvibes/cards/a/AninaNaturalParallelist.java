package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.TriggeringSpellXValue;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureRandomCreatureWithManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasXInManaCostPredicate;

import java.util.List;

@CardRegistration(set = "YSOS", collectorNumber = "14")
public class AninaNaturalParallelist extends Card {

    public AninaNaturalParallelist() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardHasXInManaCostPredicate(),
                List.of(new ConjureRandomCreatureWithManaValueEffect(
                        new TriggeringSpellXValue(), false))));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardRestrictedManaEffect(ManaColor.GREEN, 1, new ManaRestriction.XSpellCosts()),
                        new AwardRestrictedManaEffect(ManaColor.BLUE, 1, new ManaRestriction.XSpellCosts())),
                "{T}: Add {G}{U}. Spend this mana only to cast spells with {X} in their mana costs."
        ));
    }
}
