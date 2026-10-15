package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerCastTwoOrMoreSpellsThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOM", collectorNumber = "85")
public class XerexStrobeKnight extends Card {

    public XerexStrobeKnight() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new CreateTokenEffect(
                        1, "Knight", 2, 2, null,
                        Set.of(CardColor.WHITE, CardColor.BLUE), List.of(CardSubtype.KNIGHT),
                        Set.of(Keyword.VIGILANCE), Set.of())),
                "{T}: Create a 2/2 white and blue Knight creature token with vigilance. Activate only if you've cast two or more spells this turn."
        ).withActivationCondition(
                new ControllerCastTwoOrMoreSpellsThisTurn(null),
                "Activate only if you've cast two or more spells this turn."
        ));
    }
}
