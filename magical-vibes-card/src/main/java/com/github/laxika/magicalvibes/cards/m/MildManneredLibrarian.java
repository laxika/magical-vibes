package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureTypeWithBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

import java.util.List;

@CardRegistration(set = "FDN", collectorNumber = "228")
public class MildManneredLibrarian extends Card {

    public MildManneredLibrarian() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}",
                List.of(
                        BecomeCreatureTypeWithBasePowerToughnessEffect.replacingSubtype(
                                CardSubtype.WEREWOLF, null, CardSubtype.HUMAN),
                        new PutCountersOnSourceEffect(1, 1, 2),
                        new DrawCardEffect()
                ),
                "{3}{G}: This creature becomes a Werewolf. Put two +1/+1 counters on it and you draw a card. Activate only once."
        ).withMaxActivationsPerGame(1));
    }
}
