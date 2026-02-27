package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "100")
public class OxiddaDaredevil extends Card {

    public OxiddaDaredevil() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeArtifactCost(), new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF)),
                "Sacrifice an artifact: Oxidda Daredevil gains haste until end of turn."
        ));
    }
}
