package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayScryEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ONE", collectorNumber = "205")
public class KayaIntangibleSlayer extends Card {

    public KayaIntangibleSlayer() {
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(
                        new LoseLifeEffect(3, LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(3)
                ),
                "+2: Each opponent loses 3 life and you gain 3 life."
        ));

        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new DrawCardEffect(2), EachPlayerMayScryEffect.forOpponents(1)),
                "0: You draw two cards. Then each opponent may scry 1."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new ExileTargetPermanentAndCreateTokenCopyEffect(
                        List.of(CardSubtype.SPIRIT),
                        Set.of(CardType.CREATURE),
                        Set.of(Keyword.FLYING),
                        CardColor.WHITE,
                        1,
                        1,
                        true
                )),
                "−3: Exile target creature or enchantment. If it wasn't an Aura, create a token that's a copy of it, "
                        + "except it's a 1/1 white Spirit creature with flying in addition to its other types.",
                new PermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsEnchantmentPredicate()
                        )),
                        "Target must be a creature or enchantment"
                )
        ));
    }
}
