package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "CON", collectorNumber = "11")
public class MartialCoup extends Card {

    public MartialCoup() {
        // Create X 1/1 white Soldier creature tokens.
        addEffect(EffectSlot.SPELL, CreateTokenEffect.whiteSoldier(new XValue()));

        // If X is 5 or more, destroy all other creatures (the Soldier tokens just made are spared).
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new SpellXAtLeast(5),
                DestroyAllPermanentsEffect.sparingPermanentsCreatedThisResolution(new PermanentIsCreaturePredicate())));
    }
}
