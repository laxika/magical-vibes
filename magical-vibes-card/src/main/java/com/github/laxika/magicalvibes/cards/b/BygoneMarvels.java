package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellIfConditionEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "LCC", collectorNumber = "57")
@CardRegistration(set = "LCC", collectorNumber = "89")
public class BygoneMarvels extends Card {

    public BygoneMarvels() {
        GraveyardCardThreshold descendEight = new GraveyardCardThreshold(8, new CardIsPermanentPredicate());

        addEffect(EffectSlot.ON_SELF_CAST, new ConditionalEffect(descendEight,
                new CopyThisSpellIfConditionEffect(descendEight, false, false, false, 2)));
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardIsPermanentPredicate())
                .targetGraveyard(true)
                .build());
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
