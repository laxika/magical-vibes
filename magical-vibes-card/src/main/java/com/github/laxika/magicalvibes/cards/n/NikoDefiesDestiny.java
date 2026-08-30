package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.ForetoldCardsInExile;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasForetellPredicate;

@CardRegistration(set = "KHM", collectorNumber = "226")
public class NikoDefiesDestiny extends Card {

    public NikoDefiesDestiny() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new GainLifeEffect(new Scaled(new ForetoldCardsInExile(CountScope.CONTROLLER), 2)));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new AwardRestrictedManaEffect(ManaColor.WHITE, 1, new ManaRestriction.ForetellSpells()));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new AwardRestrictedManaEffect(ManaColor.BLUE, 1, new ManaRestriction.ForetellSpells()));
        addEffect(EffectSlot.SAGA_CHAPTER_III, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardHasForetellPredicate())
                .targetGraveyard(true)
                .build());
    }
}
