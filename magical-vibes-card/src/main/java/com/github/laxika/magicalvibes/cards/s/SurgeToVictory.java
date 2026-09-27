package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TargetCardsManaValueSum;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CopyImprintedCardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SOC", collectorNumber = "256")
public class SurgeToVictory extends Card {

    public SurgeToVictory() {
        CardPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));

        target(new GraveyardCardPredicateTargetFilter(
                instantOrSorcery, GraveyardSearchScope.CONTROLLERS_GRAVEYARD))
                .addEffect(EffectSlot.SPELL,
                        new ExileTargetCardFromGraveyardAndTrackWithSourceEffect(
                                instantOrSorcery, GraveyardSearchScope.CONTROLLERS_GRAVEYARD))
                .addEffect(EffectSlot.SPELL,
                        new BoostAllOwnCreaturesEffect(new TargetCardsManaValueSum(), new Fixed(0)))
                .addEffect(EffectSlot.SPELL,
                        new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                                EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                                new CopyImprintedCardAndMayCastCopyEffect(false)));
    }
}
