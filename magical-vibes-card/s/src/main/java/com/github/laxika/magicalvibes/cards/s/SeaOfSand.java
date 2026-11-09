package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RevealDrawnCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OHOP", collectorNumber = "33")
@CardRegistration(set = "OPCA", collectorNumber = "68")
public class SeaOfSand extends Card {

    public SeaOfSand() {
        // Players reveal each card they draw.
        addStaticEffect(RevealDrawnCardsEffect.eachByEveryPlayer());
        // Whenever a player draws a land card, that player gains 3 life.
        CardPredicate land = new CardTypePredicate(CardType.LAND);
        addTrigger(List.of(Triggers.youDrawCard().withFilter(land), Triggers.opponentDrawsCard().withFilter(land)),
                new GainLifeEffect(new Fixed(3), GainLifeRecipient.TRIGGERING_PLAYER));
        // Whenever a player draws a nonland card, that player loses 3 life.
        CardPredicate nonland = new CardNotPredicate(land);
        addTrigger(List.of(Triggers.youDrawCard().withFilter(nonland), Triggers.opponentDrawsCard().withFilter(nonland)),
                new LoseLifeEffect(3, LoseLifeRecipient.TRIGGERING_PLAYER));
        target(TargetFilters.permanent()).addTrigger(Triggers.chaosEnsues(), new PutTargetOnTopOfLibraryEffect());
    }
}
