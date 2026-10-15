package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GrantEscapeToGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantPlayerStaticEffectsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "LCC", collectorNumber = "50")
@CardRegistration(set = "LCC", collectorNumber = "82")
public class TheGrimCaptainsLocker extends Card {

    public TheGrimCaptainsLocker() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SurveilEffect(1)),
                "{T}: Surveil 1."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GrantPlayerStaticEffectsUntilEndOfTurnEffect(List.of(
                        new GrantEscapeToGraveyardCardsEffect(
                                new CardTypePredicate(CardType.CREATURE), "{3}{B}", 4)))),
                "{T}: Until end of turn, each creature card in your graveyard gains escape {3}{B}."
        ));
    }
}
