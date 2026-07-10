package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.FixedIfControlsAllNamed;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "327")
public class UrzasMine extends Card {

    public UrzasMine() {
        // {T}: Add {C}. If you control an Urza's Power-Plant and an Urza's Tower, add {C}{C} instead.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS,
                        new FixedIfControlsAllNamed(List.of("Urza's Power-Plant", "Urza's Tower"), 2, 1))),
                "{T}: Add {C}. If you control an Urza's Power-Plant and an Urza's Tower, add {C}{C} instead."
        ));
    }
}
