package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityToOwnLandsEffect;

import java.util.List;

@CardRegistration(set = "10E", collectorNumber = "271")
public class JoinerAdept extends Card {

    public JoinerAdept() {
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityToOwnLandsEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new AwardAnyColorManaEffect()),
                        false,
                        "{T}: Add one mana of any color."
                )
        ));
    }
}
