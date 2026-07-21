package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDyingCreatureCardAndCreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "ARB", collectorNumber = "114")
public class UnscytheKillerOfKings extends Card {

    public UnscytheKillerOfKings() {
        // Equipped creature gets +3/+3 and has first strike.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, 3, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.EQUIPPED_CREATURE));

        // Whenever a creature dealt damage by equipped creature this turn dies, you may exile that
        // card. If you do, create a 2/2 black Zombie creature token.
        addEffect(EffectSlot.ON_DAMAGED_CREATURE_DIES,
                new MayEffect(
                        new ExileDyingCreatureCardAndCreateTokenEffect(CreateTokenEffect.blackZombie(1)),
                        "Exile that creature card and create a 2/2 black Zombie?"));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
