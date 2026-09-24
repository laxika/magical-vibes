package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantWarpToChosenCardUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SeekEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YEOE", collectorNumber = "10")
public class GraviticHerald extends Card {

    public GraviticHerald() {
        CardAllOfPredicate nonlandPermanentManaValueThreeOrLess = new CardAllOfPredicate(List.of(
                new CardIsPermanentPredicate(),
                new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                new CardMaxManaValuePredicate(3)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SeekEffect(
                nonlandPermanentManaValueThreeOrLess, true));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GrantWarpToChosenCardUntilEndOfTurnEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LoseLifeEffect(2));
    }
}
