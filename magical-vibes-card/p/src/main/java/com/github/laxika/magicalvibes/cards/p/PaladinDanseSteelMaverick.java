package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "20")
@CardRegistration(set = "PIP", collectorNumber = "548")
public class PaladinDanseSteelMaverick extends Card {

    public PaladinDanseSteelMaverick() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new ExileSelfCost(),
                        new GrantKeywordEffect(
                                Keyword.INDESTRUCTIBLE,
                                GrantScope.ALL_OWN_CREATURES,
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentHasSubtypePredicate(CardSubtype.HUMAN)
                                ))
                        )
                ),
                "Exile Paladin Danse: Each creature you control that's an artifact or Human gains indestructible until end of turn."
        ));
    }
}
