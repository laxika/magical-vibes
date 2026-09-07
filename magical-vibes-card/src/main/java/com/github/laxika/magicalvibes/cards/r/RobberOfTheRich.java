package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttackedWithCreaturesOfSubtypeThisTurn;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerHasMoreCardsInHandThanController;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;

@CardRegistration(set = "ELD", collectorNumber = "138")
public class RobberOfTheRich extends Card {

    public RobberOfTheRich() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new DefendingPlayerHasMoreCardsInHandThanController(),
                new ExileTopCardsToSourceEffect(1, false, false, LibraryScope.TARGET_OPPONENT)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AttackedWithCreaturesOfSubtypeThisTurn(1, CardSubtype.ROGUE),
                new AllowCastFromCardsExiledWithSourceEffect(
                        true, null, false, false, 0, null, false, true, false)));
    }
}
