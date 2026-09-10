package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BFZ", collectorNumber = "29")
public class GideonAllyOfZendikar extends Card {

    public GideonAllyOfZendikar() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(
                        new AnimatePermanentsEffect(5, 5,
                                List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER, CardSubtype.ALLY),
                                Set.of(Keyword.INDESTRUCTIBLE)),
                        PreventDamageEffect.allToSelf()
                ),
                "+1: Until end of turn, Gideon becomes a 5/5 Human Soldier Ally creature with indestructible "
                        + "that's still a planeswalker. Prevent all damage that would be dealt to him this turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new CreateTokenEffect("Knight Ally", 2, 2, CardColor.WHITE,
                        List.of(CardSubtype.KNIGHT, CardSubtype.ALLY), Set.of(), Set.of())),
                "0: Create a 2/2 white Knight Ally creature token."
        ));

        addActivatedAbility(new ActivatedAbility(
                -4,
                List.of(new CreateEmblemEffect(
                        List.of(new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES)),
                        "Creatures you control get +1/+1.")),
                "-4: You get an emblem with \"Creatures you control get +1/+1.\""
        ));
    }
}
