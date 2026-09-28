package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardExiledWithSourceIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "289")
@CardRegistration(set = "MB2", collectorNumber = "525")
public class AlberixTheTradePlanet extends Card {

    public AlberixTheTradePlanet() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileTopCardsToSourceEffect(5, false));

        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Discard a card. If you do, put two of Alberix's resources into its owner's hand.",
                        new DiscardCardThenEffect(
                                null,
                                SequenceEffect.of(
                                        new PutCardExiledWithSourceIntoHandEffect(),
                                        new PutCardExiledWithSourceIntoHandEffect()),
                                "a card")),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile the top card of your library as a resource.",
                        new ExileTopCardsToSourceEffect(1, false)))));
    }
}
