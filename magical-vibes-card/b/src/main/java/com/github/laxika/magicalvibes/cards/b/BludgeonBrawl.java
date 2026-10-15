package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantEquipByManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "80")
public class BludgeonBrawl extends Card {

    public BludgeonBrawl() {
        PermanentPredicate filter = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNotPredicate(new PermanentIsCreaturePredicate()),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT))
        ));

        // Each noncreature, non-Equipment artifact is an Equipment
        addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(
                CardSubtype.EQUIPMENT, GrantScope.ALL_PERMANENTS, false, filter));

        // ...with equip {X} and "Equipped creature gets +X/+0," where X is that artifact's mana value
        addEffect(EffectSlot.STATIC, new GrantEquipByManaValueEffect(filter));
    }
}
