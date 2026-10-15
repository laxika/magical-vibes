package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

/** Back face of {@link com.github.laxika.magicalvibes.cards.b.BrigidClachansHeart}. */
public class BrigidDounsMind extends Card {

    public BrigidDounsMind() {
        PermanentCount otherCreatures = new PermanentCount(
                new PermanentIsCreaturePredicate(), CountScope.CONTROLLER, true);

        // {T}: Add X {G}, where X is the number of other creatures you control.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new AwardManaEffect(ManaColor.GREEN, otherCreatures)),
                "{T}: Add X {G}, where X is the number of other creatures you control."
        ));

        // {T}: Add X {W}, where X is the number of other creatures you control.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new AwardManaEffect(ManaColor.WHITE, otherCreatures)),
                "{T}: Add X {W}, where X is the number of other creatures you control."
        ));

        // At the beginning of your first main phase, you may pay {W}. If you do, transform Brigid.
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new MayPayManaEffect("{W}", new TransformSelfEffect(),
                        "Pay {W} to transform Brigid?"));
    }
}
