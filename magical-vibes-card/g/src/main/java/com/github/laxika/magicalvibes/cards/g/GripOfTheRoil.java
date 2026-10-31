package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.ControllerCastAnotherSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "56")
public class GripOfTheRoil extends Card {

    public GripOfTheRoil() {
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{1}{U}")),
                new ControllerCastAnotherSpellThisTurn(new CardTruePredicate()), false));
        target(TargetFilters.creature())
                .addSpellEffect(new TapPermanentsEffect(TapUntapScope.TARGET))
                .addSpellEffect(new SkipNextUntapEffect(TapUntapScope.TARGET))
                .addSpellEffect(new DrawCardEffect(1));
    }
}
