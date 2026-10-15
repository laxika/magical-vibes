package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentCreaturesInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMU", collectorNumber = "209")
public class NemataPrimevalWarden extends Card {

    public NemataPrimevalWarden() {
        addEffect(EffectSlot.STATIC, new ExileOpponentCreaturesInsteadOfDyingEffect(
                false, saprolingToken()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(sacrificeSaproling(), new BoostSelfEffect(2, 2)),
                "{G}, Sacrifice a Saproling: Nemata gets +2/+2 until end of turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new SacrificeMultiplePermanentsCost(2, saprolingPredicate()),
                        new DrawCardEffect()),
                "{1}{B}, Sacrifice two Saprolings: Draw a card."
        ));
    }

    private static SacrificePermanentCost sacrificeSaproling() {
        return new SacrificePermanentCost(
                saprolingPredicate(),
                "Sacrifice a Saproling"
        );
    }

    private static PermanentAllOfPredicate saprolingPredicate() {
        return new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.SAPROLING)
        ));
    }

    private static CreateTokenEffect saprolingToken() {
        return new CreateTokenEffect(
                "Saproling",
                1,
                1,
                CardColor.GREEN,
                List.of(CardSubtype.SAPROLING),
                Set.of(),
                Set.of()
        );
    }
}
