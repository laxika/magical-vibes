package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.GrantTargetGraveyardCardCastEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallySetTargetCreatureCardTypeEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YDMU", collectorNumber = "10")
public class ReezugTheBonecobbler extends Card {

    public ReezugTheBonecobbler() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{0}",
                List.of(
                        new GrantTargetGraveyardCardCastEffect(
                                new CardTypePredicate(CardType.CREATURE),
                                GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                                false),
                        new PerpetuallySetTargetCreatureCardTypeEffect(CardType.ARTIFACT)),
                "{T}: Target creature card in your graveyard perpetually becomes an artifact. "
                        + "You may cast that card this turn. (It loses all other card types.)"));
    }
}
