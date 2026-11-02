package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentCreaturesInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "MID", collectorNumber = "103")
@CardRegistration(set = "DBL", collectorNumber = "103")
public class GisaGloriousResurrector extends Card {

    public GisaGloriousResurrector() {
        addStaticEffect(ExileOpponentCreaturesInsteadOfDyingEffect.withSourceTracking());
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new ReturnAllCardsExiledWithSourceEffect(true,
                        new CardTypePredicate(CardType.CREATURE), false, Set.of(Keyword.DECAYED)));
    }
}
