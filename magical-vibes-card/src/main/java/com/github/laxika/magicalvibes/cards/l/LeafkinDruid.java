package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "178")
public class LeafkinDruid extends Card {

    public LeafkinDruid() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardManaEffect(ManaColor.GREEN),
                        new ConditionalEffect(
                                new ControlsPermanentCount(4, new PermanentIsCreaturePredicate()),
                                new AwardManaEffect(ManaColor.GREEN))),
                "{T}: Add {G}. If you control four or more creatures, add {G}{G} instead."
        ));
    }
}
