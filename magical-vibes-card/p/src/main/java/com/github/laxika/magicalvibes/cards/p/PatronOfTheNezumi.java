package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "77")
@CardRegistration(set = "PHUK", collectorNumber = "25")
@CardRegistration(set = "CMD", collectorNumber = "93")
@CardRegistration(set = "CM2", collectorNumber = "72")
public class PatronOfTheNezumi extends Card {

    public PatronOfTheNezumi() {
        addCastingOption(AlternateHandCast.offering(List.of(
                new ManaCastingCost("{5}{B}{B}"),
                new SacrificePermanentsCost(1, new PermanentHasSubtypePredicate(CardSubtype.RAT))
        )));
        // Whenever a permanent is put into an opponent's graveyard, that player loses 1 life.
        // The slot bakes the graveyard's owner as the entry's target, so TARGET_PLAYER is "that player".
        addEffect(EffectSlot.ON_PERMANENT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD,
                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER));
    }
}
