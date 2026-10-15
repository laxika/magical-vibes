package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.Set;

@CardRegistration(set = "ALA", collectorNumber = "70")
@CardRegistration(set = "M19", collectorNumber = "90")
@CardRegistration(set = "FDN", collectorNumber = "521")
@CardRegistration(set = "HOP", collectorNumber = "25")
@CardRegistration(set = "SLD", collectorNumber = "1458")
@CardRegistration(set = "MIC", collectorNumber = "111")
@CardRegistration(set = "FDC", collectorNumber = "95")
public class DeathBaron extends Card {

    public DeathBaron() {
        // Skeletons you control and other Zombies you control get +1/+1 and have deathtouch.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, Set.of(Keyword.DEATHTOUCH), GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.SKELETON, CardSubtype.ZOMBIE))));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, Set.of(Keyword.DEATHTOUCH), GrantScope.SELF,
                new PermanentHasSubtypePredicate(CardSubtype.SKELETON)));
    }
}
