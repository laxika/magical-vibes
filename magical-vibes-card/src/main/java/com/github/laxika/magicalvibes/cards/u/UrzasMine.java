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

@CardRegistration(set = "5ED", collectorNumber = "427")
@CardRegistration(set = "9ED", collectorNumber = "327")
@CardRegistration(set = "8ED", collectorNumber = "328")
@CardRegistration(set = "ATQ", collectorNumber = "83a")
@CardRegistration(set = "ATQ", collectorNumber = "83b")
@CardRegistration(set = "ATQ", collectorNumber = "83c")
@CardRegistration(set = "ATQ", collectorNumber = "83d")
@CardRegistration(set = "CHR", collectorNumber = "114a")
@CardRegistration(set = "CHR", collectorNumber = "114b")
@CardRegistration(set = "CHR", collectorNumber = "114c")
@CardRegistration(set = "CHR", collectorNumber = "114d")
public class UrzasMine extends Card {

    public UrzasMine() {
        // {T}: Add {C}. If you control an Urza's Power-Plant and an Urza's Tower, add {C}{C} instead.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS,
                        new FixedIfCondition(new AllOf(List.of(
                                new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                                        new PermanentHasSubtypePredicate(CardSubtype.URZAS),
                                        new PermanentHasSubtypePredicate(CardSubtype.POWER_PLANT)))),
                                new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                                        new PermanentHasSubtypePredicate(CardSubtype.URZAS),
                                        new PermanentHasSubtypePredicate(CardSubtype.TOWER)))))), 2, 1))),
                "{T}: Add {C}. If you control an Urza's Power-Plant and an Urza's Tower, add {C}{C} instead."
        ));
    }
}
