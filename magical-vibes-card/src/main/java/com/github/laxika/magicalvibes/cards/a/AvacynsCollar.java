package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.SubtypeConditionalEffect;

@CardRegistration(set = "DKA", collectorNumber = "145")
public class AvacynsCollar extends Card {

    public AvacynsCollar() {
        // Equipped creature gets +1/+0 and has vigilance
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 0, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.EQUIPPED_CREATURE));

        // Whenever equipped creature dies, if it was a Human, create a 1/1 white Spirit creature token with flying
        addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DIES, new SubtypeConditionalEffect(
                CardSubtype.HUMAN,
                CreateTokenEffect.whiteSpirit(1)
        ));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
