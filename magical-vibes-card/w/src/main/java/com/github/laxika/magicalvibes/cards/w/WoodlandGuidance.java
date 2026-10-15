package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LRW", collectorNumber = "243")
public class WoodlandGuidance extends Card {

    public WoodlandGuidance() {
        // Return target card from your graveyard to your hand.
        addEffect(EffectSlot.SPELL,
                ReturnCardFromGraveyardEffect.builder().destination(GraveyardChoiceDestination.HAND).targetGraveyard(true).build());

        // Clash with an opponent. If you win, untap all Forests you control.
        addEffect(EffectSlot.SPELL, new ClashEffect(
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentHasSubtypePredicate(CardSubtype.FOREST))));

        // Exile Woodland Guidance.
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
