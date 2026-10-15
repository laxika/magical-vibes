package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "PIP", collectorNumber = "192")
@CardRegistration(set = "PIP", collectorNumber = "467")
@CardRegistration(set = "PIP", collectorNumber = "720")
@CardRegistration(set = "PIP", collectorNumber = "995")
public class StolenStrategy extends Card {

    public StolenStrategy() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ExileTopCardsToSourceEffect(1, false, false, LibraryScope.EACH_OPPONENT));
        addEffect(EffectSlot.STATIC, new AllowCastFromCardsExiledWithSourceEffect(
                true,
                new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                false, false, 0, null, false, true, false));
    }
}
