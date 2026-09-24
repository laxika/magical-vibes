package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "CMM", collectorNumber = "197")
@CardRegistration(set = "CMM", collectorNumber = "526")
public class WakeTheDead extends Card {

    public WakeTheDead() {
        setSpellCastTimingRestriction(SpellCastTimingRestriction.OPPONENTS_COMBAT);

        // Return exactly X target creature cards from your graveyard. Each returned creature is
        // sacrificed at the beginning of the next end step.
        addEffect(EffectSlot.SPELL, new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                new CardTypePredicate(CardType.CREATURE),
                0, false, false, null, 0, null, null, null, 0,
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD, false, false, true));
    }
}
