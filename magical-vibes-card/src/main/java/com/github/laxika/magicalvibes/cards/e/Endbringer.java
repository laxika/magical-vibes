package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "3")
public class Endbringer extends Card {

    public Endbringer() {
        addEffect(EffectSlot.STATIC, new UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect(
                TurnStep.UNTAP, null, TapUntapScope.SELF));

        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: Endbringer deals 1 damage to any target."));

        addActivatedAbility(new ActivatedAbility(true, "{C}",
                List.of(
                        new CantAttackThisTurnEffect(TapUntapScope.TARGET),
                        new CantBlockThisTurnEffect(TapUntapScope.TARGET)),
                "{C}, {T}: Target creature can't attack or block this turn.",
                TargetFilters.creature()));

        addActivatedAbility(new ActivatedAbility(true, "{C}{C}",
                List.of(new DrawCardEffect()), "{C}{C}, {T}: Draw a card."));
    }
}
