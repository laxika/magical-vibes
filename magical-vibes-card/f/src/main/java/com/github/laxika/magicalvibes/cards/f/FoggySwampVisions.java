package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseXValueCost;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "TLA", collectorNumber = "102")
public class FoggySwampVisions extends Card {

    public FoggySwampVisions() {
        addCastingCost(new ChooseXValueCost(0, Integer.MAX_VALUE));
        addCastingCost(WaterbendCost.x());
        // Exile X target creature cards from graveyards. For each creature card exiled this way, create a token
        // that's a copy of it. At the beginning of your next end step, sacrifice those tokens.
        addSpellEffect(ExileCardsFromGraveyardEffect.exactlyXFromGraveyards(new CardTypePredicate(CardType.CREATURE)));
        addSpellEffect(CreateTokenCopyOfCardEffect.of(Subjects.cardsThisWay(StepKind.EXILE),
                new CreateTokenCopyOfTargetPermanentEffect()));
        addSpellEffect(new SacrificeCreatedPermanentsAtEndStepEffect(true));
    }
}
