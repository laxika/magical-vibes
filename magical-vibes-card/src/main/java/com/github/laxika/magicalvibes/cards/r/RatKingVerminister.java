package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.PermanentLeftBattlefieldUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureAndSameNameCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMT", collectorNumber = "71")
@CardRegistration(set = "TMT", collectorNumber = "266")
public class RatKingVerminister extends Card {

    public RatKingVerminister() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(
                        new PermanentLeftBattlefieldUnderYourControlThisTurn(),
                        SequenceEffect.of(
                                new CreateTokenEffect(1, "Rat", 1, 1, CardColor.BLACK,
                                        List.of(CardSubtype.RAT), Set.of(), Set.of()),
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE))));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeMultiplePermanentsCost(3,
                                new PermanentHasSubtypePredicate(CardSubtype.RAT)),
                        new ReturnTargetCreatureAndSameNameCardsFromGraveyardToBattlefieldEffect()),
                "{T}, Sacrifice three Rats: Return target creature card and all other cards with the same name as that card from your graveyard to the battlefield tapped."
        ));
    }
}
