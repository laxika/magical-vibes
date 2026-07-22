package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ArlinnEmbracedByTheMoonEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

/**
 * Arlinn, Embraced by the Moon — back face of Arlinn Kord.
 * Legendary Planeswalker — Arlinn (Red, Green).
 */
public class ArlinnEmbracedByTheMoon extends Card {

    public ArlinnEmbracedByTheMoon() {
        // +1: Creatures you control get +1/+1 and gain trample until end of turn.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(
                        new BoostAllOwnCreaturesEffect(1, 1),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES)
                ),
                "+1: Creatures you control get +1/+1 and gain trample until end of turn."
        ));

        // −1: Arlinn deals 3 damage to any target. Transform Arlinn.
        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(
                        new DealDamageToAnyTargetEffect(3),
                        new TransformSelfEffect()
                ),
                "\u22121: Arlinn deals 3 damage to any target. Transform Arlinn."
        ));

        // −6: You get an emblem with "Creatures you control have haste and
        // '{T}: This creature deals damage equal to its power to any target.'"
        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new ArlinnEmbracedByTheMoonEmblemEffect()),
                "\u22126: You get an emblem with \"Creatures you control have haste and "
                        + "'{T}: This creature deals damage equal to its power to any target.'\""
        ));
    }
}
