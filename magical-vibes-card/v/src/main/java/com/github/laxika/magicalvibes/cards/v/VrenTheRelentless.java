package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreaturesExiledThisTurn;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentCreaturesInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "239")
public class VrenTheRelentless extends Card {

    public VrenTheRelentless() {
        // Ward {2}.
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(2));

        addStaticEffect(new ExileOpponentCreaturesInsteadOfDyingEffect());

        PermanentCount otherRats = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.RAT), CountScope.CONTROLLER, true);
        CreateTokenEffect ratToken = new CreateTokenEffect(
                new CreaturesExiledThisTurn(CountScope.OPPONENTS),
                "Rat", 1, 1, CardColor.BLACK, List.of(CardSubtype.RAT), Set.of(), Set.of())
                .withTokenEffects(List.of(StaticAbility.of(new BoostSelfEffect(otherRats, otherRats))));
        addEffect(EffectSlot.END_STEP_TRIGGERED, ratToken);
    }
}
