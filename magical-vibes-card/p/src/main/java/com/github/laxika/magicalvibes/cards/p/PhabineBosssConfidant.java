package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NCC", collectorNumber = "9")
@CardRegistration(set = "NCC", collectorNumber = "108")
public class PhabineBosssConfidant extends Card {

    public PhabineBosssConfidant() {
        addStaticEffect(new GrantKeywordEffect(
                Keyword.HASTE,
                Subjects.creaturesYouControl(),
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsTokenPredicate()))));
        // Parley: the revealed cards are counted in the steps that follow, the Citizens created before the boost so
        // they get it too.
        CardTypePredicate land = new CardTypePredicate(CardType.LAND);
        AmountThisWay nonlandsRevealed = AmountThisWay.revealedCards(new CardNotPredicate(land));
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), SequenceEffect.of(
                new RevealTopCardOfLibraryEffect(Subjects.eachPlayer()),
                new CreateTokenEffect(1, "Citizen", 1, 1, CardColor.GREEN,
                        Set.of(CardColor.GREEN, CardColor.WHITE), List.of(CardSubtype.CITIZEN))
                        .withAmount(AmountThisWay.revealedCards(land)),
                new BoostAllOwnCreaturesEffect(nonlandsRevealed, nonlandsRevealed),
                new DrawCardEffect(1, Subjects.eachPlayer())));
    }
}
