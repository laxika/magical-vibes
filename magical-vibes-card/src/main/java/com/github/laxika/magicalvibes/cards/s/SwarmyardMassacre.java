package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLC", collectorNumber = "20")
@CardRegistration(set = "BLC", collectorNumber = "55")
public class SwarmyardMassacre extends Card {

    public SwarmyardMassacre() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                2, "Squirrel", 1, 1, CardColor.GREEN,
                List.of(CardSubtype.SQUIRREL), Set.of(), Set.of()));

        var swarmyardTypes = new PermanentHasAnySubtypePredicate(Set.of(
                CardSubtype.INSECT,
                CardSubtype.RAT,
                CardSubtype.SPIDER,
                CardSubtype.SQUIRREL
        ));
        var swarmyardCreatureCount = new PermanentCount(swarmyardTypes, CountScope.CONTROLLER);
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(
                new Scaled(swarmyardCreatureCount, -1),
                new Scaled(swarmyardCreatureCount, -1),
                new PermanentNotPredicate(swarmyardTypes)));
    }
}
