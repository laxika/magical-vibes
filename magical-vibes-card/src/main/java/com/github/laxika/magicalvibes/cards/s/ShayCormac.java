package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllProtectionUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "65")
public class ShayCormac extends Card {

    public ShayCormac() {
        PermanentNotPredicate opponentPermanent = new PermanentNotPredicate(
                new PermanentControlledBySourceControllerPredicate());
        addActivatedAbility(new ActivatedAbility(false, "{1}", List.of(
                new RemoveKeywordEffect(Keyword.HEXPROOF, GrantScope.ALL_PERMANENTS, opponentPermanent),
                new RemoveKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.ALL_PERMANENTS, opponentPermanent),
                new RemoveAllProtectionUntilEndOfTurnEffect(GrantScope.ALL_PERMANENTS, opponentPermanent),
                new RemoveKeywordEffect(Keyword.SHROUD, GrantScope.ALL_PERMANENTS, opponentPermanent),
                new RemoveKeywordEffect(Keyword.WARD, GrantScope.ALL_PERMANENTS, opponentPermanent)),
                "{1}: Permanents your opponents control lose hexproof, indestructible, protection, shroud, and ward until end of turn."));

        addEffect(EffectSlot.ON_OPPONENT_CREATURE_BECOMES_TARGET_OF_YOUR_SPELL_OR_ABILITY,
                new PutCounterOnTargetPermanentEffect(CounterType.BOUNTY, 1));
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasCountersPredicate(CounterType.BOUNTY),
                        new PutCountersOnSourceEffect(1, 1, 2)));
    }
}
