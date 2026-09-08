package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealToHandAndLoseLifeEqualToManaValueEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VOW", collectorNumber = "131")
public class SorinTheMirthless extends Card {

    public SorinTheMirthless() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new LookAtTopCardMayRevealToHandAndLoseLifeEqualToManaValueEffect()),
                "+1: Look at the top card of your library. You may reveal that card and put it into your hand. "
                        + "If you do, you lose life equal to its mana value."
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CreateTokenEffect("Vampire", 2, 3, CardColor.BLACK,
                        List.of(CardSubtype.VAMPIRE), Set.of(Keyword.FLYING, Keyword.LIFELINK), Set.of())),
                "-2: Create a 2/3 black Vampire creature token with flying and lifelink."
        ));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new DealDamageToAnyTargetEffect(13), new GainLifeEffect(13)),
                "-7: Sorin deals 13 damage to any target. You gain 13 life."
        ));
    }
}
