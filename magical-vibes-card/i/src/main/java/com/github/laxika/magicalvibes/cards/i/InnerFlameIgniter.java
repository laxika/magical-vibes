package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.NthAbilityResolutionThisTurn;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "182")
@CardRegistration(set = "MM2", collectorNumber = "120")
public class InnerFlameIgniter extends Card {

    public InnerFlameIgniter() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new BoostAllOwnCreaturesEffect(1, 0),
                        new ConditionalEffect(new NthAbilityResolutionThisTurn(3),
                                new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.creaturesYouControl(),
                                        Duration.UNTIL_END_OF_TURN))),
                "{2}{R}: Creatures you control get +1/+0 until end of turn. If this is the third time this ability has resolved this turn, creatures you control gain first strike until end of turn."
        ));
    }
}
