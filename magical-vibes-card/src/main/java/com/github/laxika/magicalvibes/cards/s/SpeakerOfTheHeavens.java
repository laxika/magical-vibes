package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M21", collectorNumber = "38")
public class SpeakerOfTheHeavens extends Card {

    public SpeakerOfTheHeavens() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new CreateTokenEffect(
                        "Angel", 4, 4, CardColor.WHITE,
                        List.of(CardSubtype.ANGEL), Set.of(Keyword.FLYING), Set.of())),
                "{T}: Create a 4/4 white Angel creature token with flying. Activate only if you have at least 7 life more than your starting life total and only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new ControllerLifeAtLeast(GameData.STARTING_LIFE_TOTAL + 7),
                "Activate only if you have at least 7 life more than your starting life total"
        ));
    }
}
