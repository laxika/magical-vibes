package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

/** Said // Done, a split spell with one mode for each half. */
@CardRegistration(set = "MH2", collectorNumber = "60")
public class SaidDone extends Card {

    public SaidDone() {
        CardEffect said = new ReturnTargetCardsFromGraveyardToHandEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY))), 1);
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Said — Return target instant or sorcery card from your graveyard to your hand",
                        said).withManaCost("{2}{U}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Done — Tap up to two target creatures. Those creatures don't untap during their controller's next untap step",
                        List.of(
                                new TapPermanentsEffect(TapUntapScope.TARGET),
                                new SkipNextUntapEffect(TapUntapScope.TARGET)),
                        TargetFilters.creature(), null, 0, 2, false, null).withManaCost("{3}{U}")
        )));
    }
}
