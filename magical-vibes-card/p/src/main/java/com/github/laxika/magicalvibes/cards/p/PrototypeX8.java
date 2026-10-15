package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTriggeringPermanentThenConjureDuplicateEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentCastBySourceControllerThisTurnPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "YEOE", collectorNumber = "24")
public class PrototypeX8 extends Card {

    public PrototypeX8() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new Scaled(new CardsInGraveyard(
                        new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER), 2)));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentCastBySourceControllerThisTurnPredicate(),
                        new SacrificeTriggeringPermanentThenConjureDuplicateEffect(
                                new CreateTokenCopyOfTargetPermanentEffect(
                                        List.of(CardSubtype.ROBOT), Set.of(CardType.ARTIFACT),
                                        null, null, Map.of()))));
    }
}
