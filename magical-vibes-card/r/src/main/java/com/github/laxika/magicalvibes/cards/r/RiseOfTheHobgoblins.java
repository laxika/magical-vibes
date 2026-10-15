package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PayXManaCreateXTokensEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EVE", collectorNumber = "145")
public class RiseOfTheHobgoblins extends Card {

    public RiseOfTheHobgoblins() {
        // When this enchantment enters, you may pay {X}. If you do, create X 1/1 red and white
        // Goblin Soldier creature tokens.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new PayXManaCreateXTokensEffect(
                new CreateTokenEffect("Goblin Soldier", 1, 1, CardColor.RED,
                        Set.of(CardColor.RED, CardColor.WHITE),
                        List.of(CardSubtype.GOBLIN, CardSubtype.SOLDIER))));

        // {R/W}: Red creatures and white creatures you control gain first strike until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{R/W}",
                List.of(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.OWN_CREATURES,
                        new PermanentColorInPredicate(Set.of(CardColor.RED, CardColor.WHITE)))),
                "{R/W}: Red creatures and white creatures you control gain first strike until end of turn."));
    }
}
