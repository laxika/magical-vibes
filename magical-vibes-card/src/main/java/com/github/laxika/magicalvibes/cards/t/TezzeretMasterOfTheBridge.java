package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsAndPutMatchingOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1701")
public class TezzeretMasterOfTheBridge extends Card {

    public TezzeretMasterOfTheBridge() {
        PermanentCount artifactsYouControl = new PermanentCount(
                new PermanentIsArtifactPredicate(), CountScope.CONTROLLER);

        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardTypePredicate(CardType.PLANESWALKER))),
                artifactsYouControl, CostModificationScope.SELF));

        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(
                        new DealDamageToPlayersEffect(artifactsYouControl, DamageRecipient.EACH_OPPONENT),
                        new GainLifeEffect(artifactsYouControl)),
                "+2: Tezzeret deals X damage to each opponent, where X is the number of artifacts you control. "
                        + "You gain X life."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(ReturnTargetCardsFromGraveyardToHandEffect.exactlyOne(
                        new CardTypePredicate(CardType.ARTIFACT))),
                "−3: Return target artifact card from your graveyard to your hand."
        ));

        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new ExileTopCardsAndPutMatchingOntoBattlefieldEffect(
                        10, new CardTypePredicate(CardType.ARTIFACT))),
                "−8: Exile the top ten cards of your library. Put all artifact cards from among them onto the battlefield."
        ));
    }
}
