package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.TriggeringSpellColorCount;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandAndApplyPerpetualPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YMKM", collectorNumber = "15")
public class ConstructionArsonist extends Card {

    public ConstructionArsonist() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                ChooseCardFromHandAndApplyPerpetualPowerToughnessEffect.incorporating(
                        instantOrSorcery,
                        "{R}",
                        DealDamageEffect.to(Subjects.eachOpponent(), 2)));

        addTrigger(Triggers.youCastSpell().withFilter(new CardIsMulticoloredPredicate()),
                BoostEffect.self(new TriggeringSpellColorCount(), new TriggeringSpellColorCount()));
    }
}
