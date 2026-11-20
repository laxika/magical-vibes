package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "HOU", collectorNumber = "145")
@CardRegistration(set = "AKR", collectorNumber = "259")
@CardRegistration(set = "MP2", collectorNumber = "53")
@CardRegistration(set = "SLD", collectorNumber = "900")
@CardRegistration(set = "2XM", collectorNumber = "216")
@CardRegistration(set = "CMM", collectorNumber = "353")
@CardRegistration(set = "CMM", collectorNumber = "590")
@CardRegistration(set = "DRC", collectorNumber = "120")
public class TheScarabGod extends Card {

    public TheScarabGod() {
        PermanentCount zombiesYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE), CountScope.CONTROLLER);

        // At the beginning of your upkeep, each opponent loses X life and you scry X,
        // where X is the number of Zombies you control.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                new LoseLifeEffect(zombiesYouControl, Subjects.eachOpponent()),
                new ScryEffect(zombiesYouControl)));

        // {2}{U}{B}: Exile target creature card from a graveyard. Create a token that's a copy of it,
        // except it's a 4/4 black Zombie.
        addActivatedAbility(new ActivatedAbility(
                false, "{2}{U}{B}",
                List.of(ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(
                                1, new CardTypePredicate(CardType.CREATURE), false),
                        CreateTokenCopyOfCardEffect.of(Subjects.cardsThisWay(StepKind.EXILE),
                                new CreateTokenCopyOfTargetPermanentEffect(List.of(), Set.of(), 4, 4, Map.of(),
                                        false, false, false, false, false, false, CardColor.BLACK, Set.of()))
                                .withCreatureTypesInstead(List.of(CardSubtype.ZOMBIE))),
                "{2}{U}{B}: Exile target creature card from a graveyard. Create a token that's a copy of it, except it's a 4/4 black Zombie."));

        // When The Scarab God dies, return it to its owner's hand at the beginning of the next end step.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new RegisterDelayedReturnCardFromGraveyardToHandEffect());
    }
}
