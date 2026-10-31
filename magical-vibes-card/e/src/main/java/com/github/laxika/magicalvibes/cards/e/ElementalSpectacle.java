package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECC", collectorNumber = "15")
@CardRegistration(set = "ECC", collectorNumber = "35")
public class ElementalSpectacle extends Card {

    public ElementalSpectacle() {
        addSpellEffect(new CreateTokenEffect(
                CardType.CREATURE, new ColorsAmongControlledPermanents(), "Elemental", 5, 5,
                CardColor.RED, Set.of(CardColor.RED, CardColor.GREEN), List.of(CardSubtype.ELEMENTAL),
                Set.of(), Set.of(), false, false, List.of(), List.of(), false, false, false, 0, Set.of()));
        addSpellEffect(new GainLifeEffect(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER)));
    }
}
