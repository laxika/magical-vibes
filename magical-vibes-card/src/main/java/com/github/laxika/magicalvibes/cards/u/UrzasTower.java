package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "429")
@CardRegistration(set = "9ED", collectorNumber = "329")
@CardRegistration(set = "8ED", collectorNumber = "330")
@CardRegistration(set = "ATQ", collectorNumber = "85a")
@CardRegistration(set = "ATQ", collectorNumber = "85b")
@CardRegistration(set = "ATQ", collectorNumber = "85c")
@CardRegistration(set = "ATQ", collectorNumber = "85d")
@CardRegistration(set = "ATQ", collectorNumber = "93")
@CardRegistration(set = "ATQ", collectorNumber = "97")
@CardRegistration(set = "CHR", collectorNumber = "116")
@CardRegistration(set = "CHR", collectorNumber = "116a")
@CardRegistration(set = "CHR", collectorNumber = "116b")
@CardRegistration(set = "CHR", collectorNumber = "116c")
@CardRegistration(set = "CHR", collectorNumber = "116d")
@CardRegistration(set = "CHR", collectorNumber = "122")
@CardRegistration(set = "CHR", collectorNumber = "124")
@CardRegistration(set = "CHR", collectorNumber = "125")
public class UrzasTower extends Card {

    public UrzasTower() {
        // {T}: Add {C}. If you control an Urza's Mine and an Urza's Power-Plant, add {C}{C}{C} instead.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS,
                        new FixedIfCondition(new AllOf(List.of(
                                new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                                        new PermanentHasSubtypePredicate(CardSubtype.URZAS),
                                        new PermanentHasSubtypePredicate(CardSubtype.MINE)))),
                                new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                                        new PermanentHasSubtypePredicate(CardSubtype.URZAS),
                                        new PermanentHasSubtypePredicate(CardSubtype.POWER_PLANT)))))), 3, 1))),
                "{T}: Add {C}. If you control an Urza's Mine and an Urza's Power-Plant, add {C}{C}{C} instead."
        ));
    }
}
