package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.EachPlayerExilesFromHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "60")
public class MindSwords extends Card {

    public MindSwords() {
        // If you control a Swamp, you may sacrifice a creature rather than pay this spell's mana cost.
        addCastingOption(new AlternateHandCast(
                List.of(new SacrificePermanentsCost(1, new PermanentIsCreaturePredicate())),
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.SWAMP)),
                false));

        // Each player exiles two cards from their hand.
        addEffect(EffectSlot.SPELL, new EachPlayerExilesFromHandEffect(2));
    }
}
