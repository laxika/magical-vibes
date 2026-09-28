package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantForetellToNonlandCardsInHandEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1242")
@CardRegistration(set = "SLX", collectorNumber = "30")
public class BohnBeguilingBalladeer extends Card {

    public BohnBeguilingBalladeer() {
        addEffect(EffectSlot.STATIC, new GrantForetellToNonlandCardsInHandEffect());
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, SpellCastTriggerEffect.nth(
                2,
                null,
                List.of(new GoadTargetCreatureUntilNextTurnEffect()),
                TargetFilters.creatureAnOpponentControls()
        ));
    }
}
