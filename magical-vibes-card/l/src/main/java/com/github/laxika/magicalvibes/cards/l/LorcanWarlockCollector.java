package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileOwnCreaturesOfSubtypeInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardFromGraveyardToBattlefieldEffect;

@CardRegistration(set = "AFC", collectorNumber = "27")
public class LorcanWarlockCollector extends Card {

    public LorcanWarlockCollector() {
        // Whenever a creature card is put into an opponent's graveyard from anywhere, you may pay
        // life equal to its mana value. If you do, put it onto the battlefield under your control.
        // It's a Warlock in addition to its other types.
        addEffect(EffectSlot.ON_CREATURE_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE,
                new MayPayLifeEffect(0,
                        new ReturnTriggeringCardFromGraveyardToBattlefieldEffect(
                                false, true, null, 0, CardSubtype.WARLOCK),
                        "Pay life equal to that card's mana value to return it to the battlefield?"));

        // If a Warlock you control would die, exile it instead.
        addEffect(EffectSlot.STATIC,
                new ExileOwnCreaturesOfSubtypeInsteadOfDyingEffect(CardSubtype.WARLOCK));
    }
}
