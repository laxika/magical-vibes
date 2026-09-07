package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DIS", collectorNumber = "116")
public class LeafdrakeRoost extends Card {

    public LeafdrakeRoost() {
        target(TargetFilters.land()).addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        "{G}{U}",
                        List.of(new CreateTokenEffect(
                                1,
                                "Drake",
                                2,
                                2,
                                CardColor.GREEN,
                                Set.of(CardColor.GREEN, CardColor.BLUE),
                                List.of(CardSubtype.DRAKE),
                                Set.of(Keyword.FLYING),
                                Set.of()
                        )),
                        "{G}{U}, {T}: Create a 2/2 green and blue Drake creature token with flying."
                ),
                GrantScope.ENCHANTED_PERMANENT
        ));
    }
}
