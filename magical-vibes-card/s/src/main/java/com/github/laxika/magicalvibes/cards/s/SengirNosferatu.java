package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

@CardRegistration(set = "TSP", collectorNumber = "128")
@CardRegistration(set = "TSR", collectorNumber = "136")
public class SengirNosferatu extends Card {

    public SengirNosferatu() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new ExileSelfCost(), batToken()),
                "{1}{B}, Exile this creature: Create a 1/2 black Bat creature token with flying. "
                        + "It has \"{1}{B}, Sacrifice this token: Return an exiled card named "
                        + "Sengir Nosferatu to the battlefield under its owner's control.\""
        ));
    }

    private static CreateTokenEffect batToken() {
        return new CreateTokenEffect(
                CardType.CREATURE,
                1,
                "Bat",
                1,
                2,
                CardColor.BLACK,
                null,
                List.of(CardSubtype.BAT),
                Set.of(Keyword.FLYING),
                Set.of(),
                false,
                false,
                List.of(),
                List.of(new ActivatedAbility(
                        false,
                        "{1}{B}",
                        List.of(new SacrificeSelfCost(),
                                ChooseCardEffect.oneOf(
                                        Subjects.exiledCards(new CardNamedPredicate("Sengir Nosferatu")),
                                        "an exiled card named Sengir Nosferatu"),
                                new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.CHOOSE))),
                        "{1}{B}, Sacrifice this token: Return an exiled card named Sengir Nosferatu "
                                + "to the battlefield under its owner's control."
                )),
                false,
                false,
                false,
                0,
                Set.of()
        );
    }
}
