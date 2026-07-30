package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AVR", collectorNumber = "217")
public class MoonsilverSpear extends Card {

    public MoonsilverSpear() {
        // Equipped creature has first strike.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.EQUIPPED_CREATURE));

        // Whenever equipped creature attacks, create a 4/4 white Angel creature token with flying.
        addEffect(EffectSlot.ON_ATTACK, new CreateTokenEffect(
                "Angel", 4, 4, CardColor.WHITE, List.of(CardSubtype.ANGEL),
                Set.of(Keyword.FLYING), Set.of()
        ));

        // Equip {4}
        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
