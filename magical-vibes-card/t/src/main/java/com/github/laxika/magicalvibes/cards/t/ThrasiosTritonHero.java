package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;

@CardRegistration(set = "SPG", collectorNumber = "16")
@CardRegistration(set = "FCA", collectorNumber = "58")
@CardRegistration(set = "C16", collectorNumber = "46")
public class ThrasiosTritonHero extends Card {

    public ThrasiosTritonHero() {
        var landCard = new CardTypePredicate(CardType.LAND);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(new ScryEffect(1), new RevealTopCardOfLibraryEffect(Subjects.you()),
                        new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(landCard)).enteringTapped(),
                        new ConditionalEffect(new NotCondition(HappenedThisWay.toACard(StepKind.REVEAL, landCard)),
                                new DrawCardEffect())),
                "{4}: Scry 1, then reveal the top card of your library. If it's a land card, put it "
                        + "onto the battlefield tapped. Otherwise, draw a card."
        ));
    }
}
