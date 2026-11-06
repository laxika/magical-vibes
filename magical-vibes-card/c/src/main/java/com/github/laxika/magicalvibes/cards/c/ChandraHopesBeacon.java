package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayCastMatchingUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;

@CardRegistration(set = "MOM", collectorNumber = "134")
@CardRegistration(set = "MOM", collectorNumber = "321")
public class ChandraHopesBeacon extends Card {

    public ChandraHopesBeacon() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)))),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new CopyTriggeringSpellEffect());

        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new AwardAnyColorManaEffect(2, true)),
                "+2: Add two mana in any combination of colors."
        ));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new ExileTopCardsMayCastMatchingUntilNextTurnEffect(
                        new Fixed(5),
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY)
                        )), false, 1)),
                "+1: Exile the top five cards of your library. Until the end of your next turn, you may cast an instant or sorcery spell from among those exiled cards."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new DealDamageToEachTargetEffect(new XValue())),
                "−X: Chandra deals X damage to each of up to two targets.",
                null,
                0,
                null,
                null,
                List.of(),
                0,
                2,
                true
        ));
    }
}
