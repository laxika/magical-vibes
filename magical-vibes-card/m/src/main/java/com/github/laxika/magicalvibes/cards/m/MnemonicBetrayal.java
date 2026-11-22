package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GRN", collectorNumber = "189")
public class MnemonicBetrayal extends Card {

    public MnemonicBetrayal() {
        addSpellEffect(new ExileGraveyardCardsEffect(GraveyardExileScope.ALL_OPPONENTS));
        addSpellEffect(AllowPlayCardsFromExileEffect.thisTurn(Subjects.cardsThisWay(StepKind.EXILE,
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND))))
                .withAnyManaType());
        addSpellEffect(RegisterDelayedStepTriggerEffect.aboutExiled(Triggers.endStep(ActivePlayer.ANY),
                        new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.cardsThisWay(StepKind.EXILE)))
                .onlyIfAnyStillExiled());
        addSpellEffect(new ExileSpellEffect());
    }
}
