package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentGainsControlOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForOwnerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldForOwnerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "C21", collectorNumber = "256")
@CardRegistration(set = "C19", collectorNumber = "56")
public class PendantOfProsperity extends Card {

    public PendantOfProsperity() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOpponentGainsControlOfSourceEffect());

        CardTypePredicate land = new CardTypePredicate(CardType.LAND);
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new DrawCardEffect(1),
                        new MayEffect(
                                new PutCardToBattlefieldEffect(land, "land"),
                                "Put a land card from your hand onto the battlefield?"),
                        new DrawCardForOwnerEffect(1),
                        new MayEffect(
                                new PutCardToBattlefieldForOwnerEffect(land, "land", false),
                                "Put a land card from your hand onto the battlefield?",
                                null,
                                MayChoicePlayer.SOURCE_OWNER)
                ),
                "{2}, {T}: Draw a card, then you may put a land card from your hand onto the battlefield. "
                        + "This artifact's owner draws a card, then that player may put a land card from their hand "
                        + "onto the battlefield."
        ));
    }
}
