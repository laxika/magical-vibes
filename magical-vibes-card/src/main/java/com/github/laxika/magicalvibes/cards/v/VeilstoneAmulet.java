package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantTargetingRestrictionToOwnCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "166")
public class VeilstoneAmulet extends Card {

    public VeilstoneAmulet() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new GrantTargetingRestrictionToOwnCreaturesUntilEndOfTurnEffect(
                        TargetingRestrictionEffect.opponentSpellsAndAbilities()))));
    }
}
