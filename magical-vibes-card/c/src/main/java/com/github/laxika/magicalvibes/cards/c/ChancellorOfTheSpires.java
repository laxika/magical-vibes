package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterOpeningHandUpkeepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NPH", collectorNumber = "31")
public class ChancellorOfTheSpires extends Card {

    public ChancellorOfTheSpires() {
        // You may reveal this card from your opening hand. If you do, at the beginning of
        // the first upkeep, each opponent mills seven cards.
        addStaticEffect(new MayEffect(
                new RegisterOpeningHandUpkeepTriggerEffect(new MillEffect(7, MillRecipient.EACH_OPPONENT)),
                "Reveal this card from your opening hand?"
        ));

        // When Chancellor of the Spires enters the battlefield, you may cast target instant
        // or sorcery card from an opponent's graveyard without paying its mana cost.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new CastTargetInstantOrSorceryFromGraveyardEffect(
                        GraveyardSearchScope.OPPONENT_GRAVEYARD, true));
    }
}
