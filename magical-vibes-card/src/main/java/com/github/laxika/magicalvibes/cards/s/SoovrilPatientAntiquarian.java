package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.PutTimeCountersOnTargetExiledCardWithSuspendEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.ExiledCardPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "YSOS", collectorNumber = "27")
public class SoovrilPatientAntiquarian extends Card {

    public SoovrilPatientAntiquarian() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PutTimeCountersOnTargetExiledCardWithSuspendEffect(2)),
                "{T}: Put two time counters on target nonland card in exile that was put there from your graveyard this turn. If it doesn't have suspend, it gains suspend.",
                new ExiledCardPredicateTargetFilter(
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                        "Target must be a nonland card in exile that was put there from your graveyard this turn.")));
    }
}
