package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.CommittedCrimeThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEachOtherControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "223")
public class OkoTheRingleader extends Card {

    public OkoTheRingleader() {
        target(TargetFilters.creatureYouControl(), 0, 1)
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        new BecomeCopyOfTargetCreatureUntilEndOfTurnEffect())
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(
                        new DrawCardEffect(2),
                        new ConditionalEffect(new CommittedCrimeThisTurn(),
                                new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                        new ConditionalEffect(new NotCondition(new CommittedCrimeThisTurn()),
                                new DiscardEffect(2, DiscardRecipient.CONTROLLER))
                ),
                "+1: Draw two cards. If you've committed a crime this turn, discard a card. "
                        + "Otherwise, discard two cards."));

        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new CreateTokenEffect("Elk", 3, 3, CardColor.GREEN,
                        List.of(CardSubtype.ELK), Set.of(), Set.of())),
                "−1: Create a 3/3 green Elk creature token."));

        addActivatedAbility(new ActivatedAbility(
                -5,
                List.of(new CreateTokenCopyOfEachOtherControlledPermanentEffect(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()))),
                "−5: For each other nonland permanent you control, create a token that's a copy of that permanent."));
    }
}
