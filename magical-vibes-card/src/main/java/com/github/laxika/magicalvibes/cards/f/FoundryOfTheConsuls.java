package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ORI", collectorNumber = "247")
public class FoundryOfTheConsuls extends Card {

    public FoundryOfTheConsuls() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {5}, {T}, Sacrifice this land: Create two 1/1 colorless Thopter artifact creature tokens with flying.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(
                        new SacrificeSelfCost(),
                        new CreateTokenEffect(2, "Thopter", 1, 1, null,
                                List.of(CardSubtype.THOPTER), Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT))
                ),
                "{5}, {T}, Sacrifice Foundry of the Consuls: Create two 1/1 colorless Thopter artifact "
                        + "creature tokens with flying."
        ));
    }
}
