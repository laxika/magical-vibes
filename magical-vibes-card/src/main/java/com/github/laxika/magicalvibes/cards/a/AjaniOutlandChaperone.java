package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AjaniUltimateEffect;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenWithColorsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TappedTargetFilter;

import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ECL", collectorNumber = "4")
public class AjaniOutlandChaperone extends Card {

    public AjaniOutlandChaperone() {
        // +1: Create a 1/1 green and white Kithkin creature token.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new CreateCreatureTokenWithColorsEffect(
                        "Kithkin", 1, 1,
                        Set.of(CardColor.GREEN, CardColor.WHITE),
                        CardColor.WHITE,
                        List.of(CardSubtype.KITHKIN)
                )),
                false,
                "+1: Create a 1/1 green and white Kithkin creature token."
        ));

        // −2: Ajani deals 4 damage to target tapped creature.
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new DealDamageToTargetCreatureEffect(4)),
                true,
                "\u22122: Ajani deals 4 damage to target tapped creature.",
                new TappedTargetFilter()
        ));

        // −8: Look at the top X cards of your library, where X is your life total. You may put
        // any number of nonland permanent cards with mana value 3 or less from among them onto
        // the battlefield. Then shuffle.
        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new AjaniUltimateEffect()),
                false,
                "\u22128: Look at the top X cards of your library, where X is your life total. You may put any number of nonland permanent cards with mana value 3 or less from among them onto the battlefield. Then shuffle."
        ));
    }
}
