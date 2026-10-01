package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.HarmonizeCast;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.MatchingCardsInLibrary;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.SeekEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "YTDM", collectorNumber = "6")
public class UrenisCounsel extends Card {

    public UrenisCounsel() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new MatchingCardsInLibrary(CountScope.CONTROLLER, new CardSubtypePredicate(CardSubtype.DRAGON))));
        addEffect(EffectSlot.SPELL, new SeekEffect(new CardSubtypePredicate(CardSubtype.DRAGON)));
        addCastingOption(new HarmonizeCast("{8}{R}{R}"));
    }
}
