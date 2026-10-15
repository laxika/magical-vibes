package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class AvatarRoku extends Card {

    public AvatarRoku() {
        addEffect(EffectSlot.ON_ATTACK, new AwardManaUntilEndOfCombatEffect(ManaColor.RED, 4));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{8}",
                List.of(new CreateTokenEffect(
                        CardType.CREATURE, 1, "Dragon", 4, 4,
                        CardColor.RED, Set.of(), List.of(CardSubtype.DRAGON),
                        Set.of(Keyword.FLYING, Keyword.FIREBENDING), Set.of(), false, false,
                        Map.of(EffectSlot.ON_ATTACK,
                                new AwardManaUntilEndOfCombatEffect(ManaColor.RED, 4)),
                        List.of(), false, false, false, 0, Set.of()
                )),
                "{8}: Create a 4/4 red Dragon creature token with flying and firebending 4."
        ));
    }
}
