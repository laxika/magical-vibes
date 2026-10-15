package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "5ED", collectorNumber = "353")
@CardRegistration(set = "4ED", collectorNumber = "297")
@CardRegistration(set = "ITP", collectorNumber = "48")
@CardRegistration(set = "RQS", collectorNumber = "47")
@CardRegistration(set = "ATQ", collectorNumber = "41")
public class BatteringRam extends Card {

    public BatteringRam() {
        // "At the beginning of combat on your turn, this creature gains banding until end of combat."
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new GrantKeywordEffect(Keyword.BANDING, GrantScope.SELF, GrantDuration.UNTIL_END_OF_COMBAT));

        // Whenever this creature becomes blocked by a Wall, destroy that Wall at end of combat.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new DestroyCombatOpponentAtEndOfCombatEffect(new PermanentHasSubtypePredicate(CardSubtype.WALL), false),
                TriggerMode.PER_BLOCKER);
    }
}
