package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MBS", collectorNumber = "97")
@CardRegistration(set = "SLD", collectorNumber = "1043")
public class TezzeretAgentOfBolas extends Card {

    public TezzeretAgentOfBolas() {
        // +1: Look at the top five cards of your library. You may reveal an artifact card
        // from among them and put it into your hand. Put the rest on the bottom of your library in any order.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(LookAtTopCardsEffect.mayRevealOneToHandRestOnBottom(5, new CardTypePredicate(CardType.ARTIFACT))),
                "+1: Look at the top five cards of your library. You may reveal an artifact card from among them and put it into your hand. Put the rest on the bottom of your library in any order."
        ));

        // −1: Target artifact becomes an artifact creature with base power and toughness 5/5.
        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new AnimatePermanentsEffect(5, 5, List.of(), Set.of(), null, Set.of(),
                        Subjects.target(), Duration.PERMANENT)),
                "\u22121: Target artifact becomes an artifact creature with base power and toughness 5/5.",
                TargetFilters.artifact()
        ));

        // −4: Target player loses X life and you gain X life, where X is twice the number of artifacts you control.
        DynamicAmount twiceArtifacts = new Scaled(
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER), 2);
        addActivatedAbility(new ActivatedAbility(
                -4,
                List.of(new LoseLifeEffect(twiceArtifacts, Subjects.targetPlayer()),
                        new GainLifeEffect(twiceArtifacts, Subjects.you())),
                "\u22124: Target player loses X life and you gain X life, where X is twice the number of artifacts you control."
        ));
    }
}
