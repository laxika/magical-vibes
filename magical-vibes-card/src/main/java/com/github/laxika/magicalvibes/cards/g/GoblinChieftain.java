package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "139")
@CardRegistration(set = "M11", collectorNumber = "141")
@CardRegistration(set = "M12", collectorNumber = "138")
@CardRegistration(set = "DDT", collectorNumber = "41")
@CardRegistration(set = "SLD", collectorNumber = "1615")
@CardRegistration(set = "SLD", collectorNumber = "2424")
@CardRegistration(set = "SPG", collectorNumber = "135")
public class GoblinChieftain extends Card {

    public GoblinChieftain() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, Set.of(Keyword.HASTE), GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.GOBLIN))));
    }
}
