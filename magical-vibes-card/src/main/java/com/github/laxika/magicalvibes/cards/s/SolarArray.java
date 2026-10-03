package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordsToCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EOC", collectorNumber = "18")
@CardRegistration(set = "EOC", collectorNumber = "38")
public class SolarArray extends Card {

    public SolarArray() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardAnyColorManaEffect(),
                        RegisterDelayedControllerSpellCastTriggerEffect.oneShotUntilConsumed(
                                new CardTypePredicate(CardType.ARTIFACT),
                                List.of(new GrantKeywordsToCastSpellEffect(Set.of(Keyword.SUNBURST))))),
                "{T}: Add one mana of any color. When you next cast an artifact spell this turn, that spell gains sunburst."
        ));
    }
}
