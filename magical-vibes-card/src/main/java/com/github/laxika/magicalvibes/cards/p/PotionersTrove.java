package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "251")
public class PotionersTrove extends Card {

    private static final CardAnyOfPredicate INSTANT_OR_SORCERY = new CardAnyOfPredicate(List.of(
            new CardTypePredicate(CardType.INSTANT),
            new CardTypePredicate(CardType.SORCERY)
    ));

    public PotionersTrove() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(1)),
                "{T}: Add one mana of any color."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GainLifeEffect(2)),
                "{T}: You gain 2 life. Activate only if you've cast an instant or sorcery spell this turn."
        ).withActivationCondition(
                new ControllerCastSpellThisTurn(INSTANT_OR_SORCERY),
                "Activate only if you've cast an instant or sorcery spell this turn."
        ));
    }
}
