package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.CommanderCastsFromCommandZoneThisGame;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "C18", collectorNumber = "30")
public class GenesisStorm extends Card {

    public GenesisStorm() {
        var match = new CardAllOfPredicate(List.of(
                new CardIsPermanentPredicate(), new CardNotPredicate(new CardTypePredicate(CardType.LAND))));
        // When you cast this spell, copy it for each time you've cast your commander from the
        // command zone this game.
        addTrigger(Triggers.youCastThisSpell(),
                new CopyTriggeringSpellEffect().withCopies(new CommanderCastsFromCommandZoneThisGame()));

        // Reveal cards from the top of your library until you reveal a nonland permanent card.
        addSpellEffect(new RevealCardsFromTopUntilEffect(match));
        addSpellEffect(new ConditionalEffect(HappenedThisWay.toACard(StepKind.REVEAL, match),
                        new MayEffect(new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(match)),
                        "Put the revealed card onto the battlefield?")));
        addSpellEffect(PutCardsIntoLibraryEffect.onBottomInRandomOrder(Subjects.revealedThisWay()));
    }
}
