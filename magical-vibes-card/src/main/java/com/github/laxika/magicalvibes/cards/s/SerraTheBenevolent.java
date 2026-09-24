package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageLifeFloorEffect;
import com.github.laxika.magicalvibes.model.effect.LifeFloorCondition;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "995")
@CardRegistration(set = "SLD", collectorNumber = "1252")
@CardRegistration(set = "MH1", collectorNumber = "26")
public class SerraTheBenevolent extends Card {

    public SerraTheBenevolent() {
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new BoostAllOwnCreaturesEffect(1, 1,
                        new PermanentHasKeywordPredicate(Keyword.FLYING))),
                "+2: Creatures you control with flying get +1/+1 until end of turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new CreateTokenEffect("Angel", 4, 4, CardColor.WHITE,
                        List.of(CardSubtype.ANGEL), Set.of(Keyword.FLYING, Keyword.VIGILANCE), Set.of())),
                "-3: Create a 4/4 white Angel creature token with flying and vigilance."
        ));

        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new CreateEmblemEffect(
                        List.of(new DamageLifeFloorEffect(1, LifeFloorCondition.CONTROLS_A_CREATURE)),
                        "If you control a creature, damage that would reduce your life total to less than 1 "
                                + "reduces it to 1 instead.")),
                "-6: You get an emblem with \"If you control a creature, damage that would reduce your life "
                        + "total to less than 1 reduces it to 1 instead.\""
        ));
    }
}
