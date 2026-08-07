package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M12", collectorNumber = "78")
@CardRegistration(set = "M15", collectorNumber = "81")
@CardRegistration(set = "ORI", collectorNumber = "81")
public class TurnToFrog extends Card {

    public TurnToFrog() {
        // Until end of turn, target creature loses all abilities and becomes a blue
        // Frog with base power and toughness 1/1.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new LosesAllAbilitiesEffect(GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN))
                .addEffect(EffectSlot.SPELL, new GrantColorUntilEndOfTurnEffect(CardColor.BLUE))
                .addEffect(EffectSlot.SPELL, new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.FROG))
                .addEffect(EffectSlot.SPELL, new SetBasePowerToughnessEffect(1, 1));
    }
}
