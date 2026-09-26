package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastCardsExiledWithSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "LTC", collectorNumber = "27")
@CardRegistration(set = "LTC", collectorNumber = "110")
public class LobeliaDefenderOfBagEnd extends Card {

    public LobeliaDefenderOfBagEnd() {
        // When Lobelia enters, exile the top card of each opponent's library face down, tracked
        // with Lobelia.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTopCardsToSourceEffect(1, true, false, LibraryScope.EACH_OPPONENT));

        // {T}, Sacrifice an artifact: choose between a free play from Lobelia's exiled cards and
        // draining each opponent.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact"),
                        new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption(
                                        "Until end of turn, you may play a card exiled with Lobelia without paying its mana cost",
                                        new AllowCastCardsExiledWithSourceUntilEndOfTurnEffect(null, true)),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Each opponent loses 2 life and you gain 2 life",
                                        List.of(
                                                new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT),
                                                new GainLifeEffect(2))))
                )),
                "{T}, Sacrifice an artifact: Choose one — Until end of turn, you may play a card exiled with Lobelia without paying its mana cost. Each opponent loses 2 life and you gain 2 life."
        ));
    }
}
