package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardIntoLibraryAtPositionEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "33")
public class CalimDjinnEmperor extends Card {

    public CalimDjinnEmperor() {
        var otherCalim = new CardAllOfPredicate(List.of(
                new CardNamedPredicate("Calim, Djinn Emperor"),
                new CardNotPredicate(new CardIsSelfPredicate())));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new DrawCardEffect(1),
                        new MayEffect(
                                new ExileNCardsFromGraveyardThenEffect(
                                        2,
                                        otherCalim,
                                        new ReturnSourceCardFromGraveyardToBattlefieldEffect(true)),
                                "Exile two other cards named Calim, Djinn Emperor from your graveyard?")),
                "Calim's Breath — {1}{U}, Discard Calim: Tap up to one target nonland permanent. "
                        + "Draw a card. Then you may exile two other cards named Calim, Djinn Emperor "
                        + "from your graveyard. When you do, return Calim from your graveyard to the "
                        + "battlefield tapped.",
                TargetFilters.nonlandPermanent(),
                null,
                null,
                null,
                List.of(),
                0,
                1));

        addEffect(EffectSlot.ON_SELF_DISCARDED,
                new ConjureCardIntoLibraryAtPositionEffect(CalimDjinnEmperor::new, 6));
    }
}
