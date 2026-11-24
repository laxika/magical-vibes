package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DelveCost;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "87")
@CardRegistration(set = "SLD", collectorNumber = "1396")
@CardRegistration(set = "UMA", collectorNumber = "117")
@CardRegistration(set = "TSR", collectorNumber = "333")
@CardRegistration(set = "PIO", collectorNumber = "113")
@CardRegistration(set = "TDC", collectorNumber = "197")
public class TasigurTheGoldenFang extends Card {

    public TasigurTheGoldenFang() {
        addCastingCost(new DelveCost());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G/U}{G/U}",
                List.of(
                        new MillEffect(2, Subjects.you()),
                        ChoosePlayerEffect.opponent(),
                        ChooseCardEffect.oneOf(Subjects.cardsInYourGraveyard(
                                                new CardNotPredicate(new CardTypePredicate(CardType.LAND))),
                                        "a nonland card in your opponent's graveyard")
                                .by(Subjects.playersThisWay(StepKind.CHOOSE)),
                        new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE))
                ),
                "{2}{G/U}{G/U}: Mill two cards, then return a nonland card of an opponent's choice from your graveyard to your hand.")
        );
    }
}
