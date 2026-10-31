package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "203")
public class RonaDiscipleOfGix extends Card {

    public RonaDiscipleOfGix() {
        // When Rona, Disciple of Gix enters the battlefield, you may exile target historic
        // card from your graveyard. (Artifacts, legendaries, and Sagas are historic.)
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new ExileTargetCardFromGraveyardAndImprintOnSourceEffect(
                        new CardIsHistoricPredicate(), GraveyardSearchScope.CONTROLLERS_GRAVEYARD),
                "Exile a historic card from your graveyard?"));

        // You may cast spells from among cards exiled with Rona.
        addStaticEffect(new AllowCastFromCardsExiledWithSourceEffect(
                false, new CardNotPredicate(new CardTypePredicate(CardType.LAND)), false, false, 0));

        // {4}, {T}: Exile the top card of your library.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new ExileTopCardOfOwnLibraryEffect(true)),
                "{4}, {T}: Exile the top card of your library."
        ));
    }
}
