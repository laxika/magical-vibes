package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "58")
public class SkorpekhLord extends Card {

    public SkorpekhLord() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                1, 0, Set.of(Keyword.MENACE), GrantScope.OWN_CREATURES,
                new PermanentIsArtifactPredicate()));

        addUnearth("{2}{B}");
    }
}
