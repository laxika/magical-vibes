package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.ActivePlayerRevealsUntilCreatureToBattlefieldRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerControlsMoreCreaturesThanControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;

@CardRegistration(set = "EXO", collectorNumber = "115")
@CardRegistration(set = "TPR", collectorNumber = "184")
public class OathOfDruids extends Card {

    public OathOfDruids() {
        // At the beginning of each player's upkeep, that player chooses target player who controls
        // more creatures than they do and is their opponent. The first player may reveal cards from
        // the top of their library until they reveal a creature card, putting it onto the battlefield
        // and the rest into their graveyard.
        target(new PlayerPredicateTargetFilter(
                new PlayerControlsMoreCreaturesThanControllerPredicate(),
                "Target player must be an opponent who controls more creatures than you"
        )).addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new MayEffect(
                new ActivePlayerRevealsUntilCreatureToBattlefieldRestToGraveyardEffect(),
                "Reveal cards from the top of your library until you reveal a creature card?",
                null,
                MayChoicePlayer.ACTIVE_PLAYER));
    }
}
