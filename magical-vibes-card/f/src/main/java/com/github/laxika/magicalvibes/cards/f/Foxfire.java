package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "5ED", collectorNumber = "295")
@CardRegistration(set = "ICE", collectorNumber = "238")
public class Foxfire extends Card {

    public Foxfire() {
        // "Untap target attacking creature. Prevent all combat damage that would be dealt to and dealt by
        // that creature this turn." Same target group; both prevention effects are combat-only.
        target(TargetFilters.attackingCreature()).addEffect(EffectSlot.SPELL, new UntapPermanentsEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombatToTargetCreatures())
                .addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombatByTargetCreatures());

        // "Draw a card at the beginning of the next turn's upkeep."
        addEffect(EffectSlot.SPELL, new RegisterDrawCardsAtNextUpkeepEffect());
    }
}
