package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "ODY", collectorNumber = "192")
public class Epicenter extends Card {

    public Epicenter() {
        PermanentIsLandPredicate land = new PermanentIsLandPredicate();
        GraveyardCardThreshold threshold = new GraveyardCardThreshold(7, null);

        // "Instead" picks one clause when the spell starts resolving. The threshold clause goes first: the
        // lands it sacrifices only add cards to the graveyard, so the non-threshold clause's check still
        // sees the threshold state from before any sacrifice.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                threshold,
                new SacrificeEachMatchingPermanentEffect(land)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new NotCondition(threshold),
                new SacrificePermanentsEffect(1, land, SacrificeRecipient.TARGET_PLAYER)));
    }
}
