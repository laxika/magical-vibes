package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreatedTokensFightDifferentOpponentCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CMM", collectorNumber = "287")
@CardRegistration(set = "CMM", collectorNumber = "558")
@CardRegistration(set = "C15", collectorNumber = "36")
@CardRegistration(set = "C21", collectorNumber = "188")
@CardRegistration(set = "DSC", collectorNumber = "178")
@CardRegistration(set = "FDC", collectorNumber = "203")
public class EzurisPredation extends Card {

    public EzurisPredation() {
        addSpellEffect(new CreateTokenEffect(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.OPPONENTS),
                "Phyrexian Beast", 4, 4, CardColor.GREEN,
                List.of(CardSubtype.PHYREXIAN, CardSubtype.BEAST), Set.of(), Set.of()));
        addSpellEffect(new CreatedTokensFightDifferentOpponentCreaturesEffect());
    }
}
