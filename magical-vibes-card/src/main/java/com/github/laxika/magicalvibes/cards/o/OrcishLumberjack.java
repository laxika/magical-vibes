package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "210")
public class OrcishLumberjack extends Card {

    public OrcishLumberjack() {
        // {T}, Sacrifice a Forest: Add three mana in any combination of {R} and/or {G}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.FOREST),
                                "Sacrifice a Forest",
                                false),
                        new AwardManaOfColorsEffect(List.of(ManaColor.RED, ManaColor.GREEN), 3)),
                "{T}, Sacrifice a Forest: Add three mana in any combination of {R} and/or {G}."
        ));
    }
}
