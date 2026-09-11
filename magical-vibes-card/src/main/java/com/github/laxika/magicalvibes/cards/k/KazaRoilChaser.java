package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForNextSpellOfTypesThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZNR", collectorNumber = "225")
public class KazaRoilChaser extends Card {

    public KazaRoilChaser() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ReduceCastCostForNextSpellOfTypesThisTurnEffect(
                        Set.of(CardType.INSTANT, CardType.SORCERY),
                        new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.WIZARD), CountScope.CONTROLLER)
                )),
                "{T}: The next instant or sorcery spell you cast this turn costs {X} less to cast, where X is the number of Wizards you control as this ability resolves."
        ));
    }
}
