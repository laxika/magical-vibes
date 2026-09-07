package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PLC", collectorNumber = "123")
public class SkirkShaman extends Card {

    public SkirkShaman() {
        addEffect(EffectSlot.STATIC, new CanBeBlockedOnlyByFilterEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentColorInPredicate(Set.of(CardColor.RED))
                )),
                "artifact creatures or red creatures"
        ));
    }
}
