package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "RAV", collectorNumber = "236")
public class TolsimirWolfblood extends Card {

    public TolsimirWolfblood() {
        // Other green creatures you control get +1/+1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.GREEN))));

        // Other white creatures you control get +1/+1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.WHITE))));

        // {T}: Create Voja, a legendary 2/2 green and white Wolf creature token.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new CreateTokenEffect(
                        CardType.CREATURE, 1, "Voja", 2, 2,
                        CardColor.GREEN, Set.of(CardColor.GREEN, CardColor.WHITE),
                        List.of(CardSubtype.WOLF), Set.of(), Set.of(),
                        false, false, Map.of(), List.of(), false, false, true, 0, Set.of())),
                "{T}: Create Voja, a legendary 2/2 green and white Wolf creature token."));
    }
}
