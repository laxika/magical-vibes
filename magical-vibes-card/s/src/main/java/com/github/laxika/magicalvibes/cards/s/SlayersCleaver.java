package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByMatchingCreatureIfAbleEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "EMN", collectorNumber = "198")
public class SlayersCleaver extends Card {

    public SlayersCleaver() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, 1, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new MustBeBlockedByMatchingCreatureIfAbleEffect(
                new PermanentHasSubtypePredicate(CardSubtype.ELDRAZI)));
        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
