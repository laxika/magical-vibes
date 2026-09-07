package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MillControllerThenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentControllerConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.Set;

@CardRegistration(set = "TMT", collectorNumber = "154")
public class TheLastRonin extends Card {

    public TheLastRonin() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate()));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new MillControllerThenEffect(4,
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .targetGraveyard(true)
                        .build()));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                EffectSlot.ON_ANY_CREATURE_ATTACKS,
                new TriggeringPermanentControllerConditionalEffect(
                        new ConditionalEffect(new AttacksAlone(), SequenceEffect.of(
                                new PutCountersOnSourceEffect(1, 1, 3),
                                new BoostSelfEffect(3, 3),
                                new GrantKeywordEffect(Set.of(
                                        Keyword.TRAMPLE,
                                        Keyword.LIFELINK,
                                        Keyword.INDESTRUCTIBLE), GrantScope.SELF))))));
    }
}
