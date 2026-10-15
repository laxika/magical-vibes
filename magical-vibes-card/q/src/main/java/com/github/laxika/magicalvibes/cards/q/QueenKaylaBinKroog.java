package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardHandThenDrawAndReturnArtifactOrCreatureCardsEffect;
import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "218")
public class QueenKaylaBinKroog extends Card {

    public QueenKaylaBinKroog() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new DiscardHandThenDrawAndReturnArtifactOrCreatureCardsEffect()),
                "{4}, {T}: Discard all the cards in your hand, then draw that many cards. You may choose an artifact or creature card with mana value 1 you discarded this way, then do the same for artifact or creature cards with mana values 2 and 3. Return those cards to the battlefield. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
