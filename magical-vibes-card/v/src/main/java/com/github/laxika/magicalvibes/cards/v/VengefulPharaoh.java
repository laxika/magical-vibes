package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromGraveyardIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M12", collectorNumber = "116")
public class VengefulPharaoh extends Card {

    public VengefulPharaoh() {
        // Whenever combat damage is dealt to you or a planeswalker you control, if this card is in
        // your graveyard, destroy target attacking creature, then put this card on top of your
        // library. The intervening-if is re-checked on resolution (CR 603.4), so the ability does
        // nothing if the card left the graveyard after the trigger.
        target(TargetFilters.attackingCreature())
                .addEffect(EffectSlot.GRAVEYARD_ON_COMBAT_DAMAGE_TO_YOU_OR_YOUR_PLANESWALKER,
                        new ConditionalEffect(new SourceCardInGraveyard(), new DestroyTargetPermanentEffect()));
        addEffect(EffectSlot.GRAVEYARD_ON_COMBAT_DAMAGE_TO_YOU_OR_YOUR_PLANESWALKER,
                new PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(0));
    }
}
