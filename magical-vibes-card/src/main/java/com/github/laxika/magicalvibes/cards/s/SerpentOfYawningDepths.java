package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1489")
public class SerpentOfYawningDepths extends Card {

    public SerpentOfYawningDepths() {
        PermanentAnyOfPredicate seaMonster = new PermanentAnyOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.KRAKEN),
                new PermanentHasSubtypePredicate(CardSubtype.LEVIATHAN),
                new PermanentHasSubtypePredicate(CardSubtype.OCTOPUS),
                new PermanentHasSubtypePredicate(CardSubtype.SERPENT)
        ));

        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new CanBeBlockedOnlyByFilterEffect(seaMonster,
                        "Krakens, Leviathans, Octopuses, and Serpents"),
                GrantScope.ALL_OWN_CREATURES,
                seaMonster));
    }
}
