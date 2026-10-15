package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardTypesAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseRandomOpponentMustAttackThisCombatEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "DSC", collectorNumber = "36")
@CardRegistration(set = "DSC", collectorNumber = "63")
public class UrsineMonstrosity extends Card {

    public UrsineMonstrosity() {
        CardTypesAmongCardsInGraveyard cardTypes = new CardTypesAmongCardsInGraveyard();
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, SequenceEffect.of(
                new MillEffect(1, MillRecipient.CONTROLLER),
                new ChooseRandomOpponentMustAttackThisCombatEffect(),
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF),
                new BoostSelfEffect(cardTypes, cardTypes)));
    }
}
