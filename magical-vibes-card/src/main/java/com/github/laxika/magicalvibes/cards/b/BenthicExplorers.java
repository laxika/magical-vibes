package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfTypeUntappedLandCouldProduceEffect;
import com.github.laxika.magicalvibes.model.effect.UntapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "24a")
@CardRegistration(set = "ALL", collectorNumber = "24b")
public class BenthicExplorers extends Card {

    public BenthicExplorers() {
        // {T}, Untap a tapped land an opponent controls: Add one mana of any type that land could produce.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new UntapMultiplePermanentsCost(1, new PermanentIsLandPredicate(), false, true),
                        new AwardManaOfTypeUntappedLandCouldProduceEffect()),
                "{T}, Untap a tapped land an opponent controls: Add one mana of any type that land could produce."));
    }
}
