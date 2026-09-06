package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ORI", collectorNumber = "151")
@CardRegistration(set = "ONS", collectorNumber = "205")
public class GoblinPiledriver extends Card {

    public GoblinPiledriver() {
        // Protection from blue.
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.BLUE)));

        // Whenever this creature attacks, it gets +2/+0 until end of turn for each other attacking Goblin.
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(
                new Scaled(
                        new PermanentCount(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsAttackingPredicate(),
                                        new PermanentHasSubtypePredicate(CardSubtype.GOBLIN))),
                                CountScope.ANY_PLAYER,
                                true),
                        2),
                new Fixed(0)));
    }
}
