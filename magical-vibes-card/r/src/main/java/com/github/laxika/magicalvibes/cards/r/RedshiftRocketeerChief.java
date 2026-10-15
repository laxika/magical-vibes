package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "218")
public class RedshiftRocketeerChief extends Card {

    public RedshiftRocketeerChief() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(
                        new SourcePower(), ManaSpendRestriction.ABILITIES, null, false)),
                "{T}: Add X mana of any one color, where X is Redshift's power. Spend this mana only to activate abilities."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{10}{R}{G}",
                List.of(PutCardToBattlefieldEffect.anyNumber(new CardIsPermanentPredicate(), "permanent")),
                "Exhaust — {10}{R}{G}: Put any number of permanent cards from your hand onto the battlefield."
                        + " (Activate each exhaust ability only once.)"
        ).withMaxActivationsPerGame(1).withExhaust());
    }
}
