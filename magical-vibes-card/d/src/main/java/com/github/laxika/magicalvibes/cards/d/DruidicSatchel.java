package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "M12", collectorNumber = "207")
@CardRegistration(set = "C13", collectorNumber = "242")
@CardRegistration(set = "C21", collectorNumber = "241")
public class DruidicSatchel extends Card {

    public DruidicSatchel() {
        var creatureCard = new CardTypePredicate(CardType.CREATURE);
        var landCard = new CardTypePredicate(CardType.LAND);
        // {2}, {T}: Reveal the top card of your library. If it's a creature card, create a 1/1 green
        // Saproling creature token. If it's a land card, put that card onto the battlefield under your
        // control. If it's a noncreature, nonland card, you gain 2 life.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new RevealTopCardOfLibraryEffect(Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.toACard(StepKind.REVEAL, creatureCard),
                                new CreateTokenEffect(1, "Saproling", 1, 1,
                                        CardColor.GREEN, List.of(CardSubtype.SAPROLING), Set.of(), Set.of())),
                        new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(landCard)).underControlOf(Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.toACard(StepKind.REVEAL, new CardAllOfPredicate(List.of(new CardNotPredicate(creatureCard), new CardNotPredicate(landCard)))),
                                new GainLifeEffect(2))),
                "{2}, {T}: Reveal the top card of your library. If it's a creature card, create a 1/1 green "
                        + "Saproling creature token. If it's a land card, put that card onto the battlefield "
                        + "under your control. If it's a noncreature, nonland card, you gain 2 life."
        ));
    }
}
