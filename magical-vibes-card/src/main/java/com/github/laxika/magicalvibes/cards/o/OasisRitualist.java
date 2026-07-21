package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "124")
public class OasisRitualist extends Card {

    public OasisRitualist() {
        // {T}: Add one mana of any color.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect()),
                "{T}: Add one mana of any color."
        ));

        // {T}, Exert this creature: Add two mana of any one color.
        // Exert ("won't untap during your next untap step") is SkipNextUntapEffect(SELF).
        // Because this produces mana it is a mana ability (CR 605.1a); both effects resolve
        // inline in ActivatedAbilityExecutionService.doResolveManaAbility.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SkipNextUntapEffect(TapUntapScope.SELF),
                        new AwardAnyColorManaEffect(2)
                ),
                "{T}, Exert this creature: Add two mana of any one color."
        ));
    }
}
