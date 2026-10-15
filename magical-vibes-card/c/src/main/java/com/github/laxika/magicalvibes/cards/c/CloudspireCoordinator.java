package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentsEnteredBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PowerBoostForCrewAndSaddleEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DFT", collectorNumber = "196")
public class CloudspireCoordinator extends Card {

    public CloudspireCoordinator() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(2));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new CreateTokenEffect(
                        CardType.CREATURE,
                        new PermanentsEnteredBattlefieldThisTurn(
                                new CardAnyOfPredicate(List.of(
                                        new CardSubtypePredicate(CardSubtype.MOUNT),
                                        new CardSubtypePredicate(CardSubtype.VEHICLE))),
                                CountScope.CONTROLLER),
                        "Pilot",
                        1,
                        1,
                        null,
                        null,
                        List.of(CardSubtype.PILOT),
                        Set.of(),
                        Set.of(),
                        false,
                        false,
                        Map.of(EffectSlot.STATIC, new PowerBoostForCrewAndSaddleEffect(2)),
                        List.of(),
                        false,
                        false,
                        false,
                        0,
                        Set.of())),
                "{T}: Create X 1/1 colorless Pilot creature tokens, where X is the number of Mounts and/or Vehicles that entered the battlefield under your control this turn."
        ));
    }
}
