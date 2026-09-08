package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ExileNCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "14")
public class ElspethSunsNemesis extends Card {

    public ElspethSunsNemesis() {
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new BoostTargetCreatureEffect(2, 1)),
                "−1: Up to two target creatures you control each get +2/+1 until end of turn.",
                null, -1, null, null,
                List.<TargetFilter>of(TargetFilters.creatureYouControl(), TargetFilters.creatureYouControl()), 0, 2));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CreateTokenEffect(2, "Human Soldier", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER), Set.of(), Set.of())),
                "−2: Create two 1/1 white Human Soldier creature tokens."));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new GainLifeEffect(5)),
                "−3: You gain 5 life."));

        addCastingOption(new GraveyardCast(null, "{4}{W}{W}",
                List.of(new ExileNCardsFromGraveyardCastingCost(null, "other cards", 4)),
                null, false, true));
    }
}
