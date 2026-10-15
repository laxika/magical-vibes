package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "124")
@CardRegistration(set = "AKR", collectorNumber = "205")
public class OasisRitualist extends Card {

    public OasisRitualist() {
        // {T}: Add one mana of any color.
        addActivatedAbility(ManaAbilities.tapForAnyColor());

        // {T}, Exert this creature: Add two mana of any one color.
        // Exert is paid as part of the activation cost (SkipNextUntapEffect with activationCost).
        // Because this produces mana it is a mana ability (CR 605.1a) and resolves inline.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SkipNextUntapEffect(TapUntapScope.SELF, null, 1, false, false, true, true),
                        new AwardAnyColorManaEffect(2)
                ),
                "{T}, Exert this creature: Add two mana of any one color."
        ));
    }
}
