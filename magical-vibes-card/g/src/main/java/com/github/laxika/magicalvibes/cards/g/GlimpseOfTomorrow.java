package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MorphicTideEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "129")
public class GlimpseOfTomorrow extends Card {

    public GlimpseOfTomorrow() {
        addActivatedAbility(AbilityZones.HAND, new ActivatedAbility(
                false,
                "{R}{R}",
                List.of(),
                "Suspend 3—{R}{R}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(3));

        CardSubtypePredicate aura = new CardSubtypePredicate(CardSubtype.AURA);
        addSpellEffect(new MorphicTideEffect(Subjects.you()));
        addSpellEffect(new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(
                new CardAllOfPredicate(List.of(new CardIsPermanentPredicate(), new CardNotPredicate(aura))))));
        addSpellEffect(new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(aura)));
        addSpellEffect(PutCardsIntoLibraryEffect.onBottomInRandomOrder(Subjects.revealedThisWay()));
    }
}
