package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PlayersHaveNoMaximumHandSizeEffect;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "46")
public class FolioOfFancies extends Card {

    public FolioOfFancies() {
        addEffect(EffectSlot.STATIC, new PlayersHaveNoMaximumHandSizeEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}{X}",
                List.of(new EachPlayerDrawsCardEffect(new XValue())),
                "{X}{X}, {T}: Each player draws X cards."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{U}",
                List.of(new MillEffect(new CardsInHand(CountScope.OPPONENTS), MillRecipient.EACH_OPPONENT)),
                "{2}{U}, {T}: Each opponent mills cards equal to the number of cards in their hand."
        ));
    }
}
