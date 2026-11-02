package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SkipKind;
import com.github.laxika.magicalvibes.model.effect.SkipNextEffect;
import com.github.laxika.magicalvibes.model.effect.SkipRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CHK", collectorNumber = "242")
public class ShisatoWhisperingHunter extends Card {

    public ShisatoWhisperingHunter() {
        // At the beginning of your upkeep, sacrifice a Snake. Mandatory; Shisato is itself a Snake,
        // so with no other Snake around it sacrifices itself.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new SacrificePermanentsEffect(
                1,
                new PermanentHasSubtypePredicate(CardSubtype.SNAKE),
                SacrificeRecipient.CONTROLLER));

        // Whenever this creature deals combat damage to a player, that player skips their next
        // untap step. The damaged player is bound as the trigger's target, not chosen.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new SkipNextEffect(SkipKind.UNTAP_STEP, SkipRecipient.DAMAGED_PLAYER));
    }
}
