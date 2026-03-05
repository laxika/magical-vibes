package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMillsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "NPH", collectorNumber = "31")
public class ChancellorOfTheSpires extends Card {

    public ChancellorOfTheSpires() {
        // You may reveal this card from your opening hand. If you do, at the beginning of
        // the first upkeep, each opponent mills seven cards.
        addEffect(EffectSlot.ON_OPENING_HAND_REVEAL, new MayEffect(
                new EachOpponentMillsEffect(7),
                "Reveal this card from your opening hand?"
        ));

        // When Chancellor of the Spires enters the battlefield, you may cast target instant
        // or sorcery card from an opponent's graveyard without paying its mana cost.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CastTargetInstantOrSorceryFromGraveyardEffect(
                        GraveyardSearchScope.OPPONENT_GRAVEYARD, true));
    }
}
