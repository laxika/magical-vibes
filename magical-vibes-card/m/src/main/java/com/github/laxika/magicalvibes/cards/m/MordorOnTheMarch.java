package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LTC", collectorNumber = "512")
@CardRegistration(set = "LTC", collectorNumber = "556")
public class MordorOnTheMarch extends Card {

    public MordorOnTheMarch() {
        addSpellEffect(ExileGraveyardCardsEffect.fromOwnGraveyard(1, new CardTypePredicate(CardType.CREATURE)));
        addSpellEffect(CreateTokenCopyOfCardEffect.of(Subjects.cardsThisWay(StepKind.EXILE),
                new CreateTokenCopyOfTargetPermanentEffect(true, true).withHasteUntilEndOfTurn()));
        addTrigger(Triggers.youCastThisSpell(), CopyTriggeringSpellEffect.storm());
    }
}
