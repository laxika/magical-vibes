package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardTypesAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseRandomOpponentMustAttackThisCombatEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSC", collectorNumber = "36")
@CardRegistration(set = "DSC", collectorNumber = "63")
public class UrsineMonstrosity extends Card {

    public UrsineMonstrosity() {
        CardTypesAmongCardsInGraveyard cardTypes = new CardTypesAmongCardsInGraveyard();
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), SequenceEffect.of(
                new MillEffect(1, Subjects.you()),
                new ChooseRandomOpponentMustAttackThisCombatEffect(),
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, Subjects.source()),
                BoostEffect.self(cardTypes, cardTypes)));
    }
}
