package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "M14", collectorNumber = "10")
public class BraveTheElements extends Card {

    public BraveTheElements() {
        // Choose a color. White creatures you control gain protection from the chosen color
        // until end of turn. Untargeted — the set of white creatures is checked on resolution.
        addEffect(EffectSlot.SPELL, new GrantProtectionChoiceUntilEndOfTurnEffect(
                GrantScope.OWN_CREATURES, new PermanentColorInPredicate(Set.of(CardColor.WHITE))));
    }
}
