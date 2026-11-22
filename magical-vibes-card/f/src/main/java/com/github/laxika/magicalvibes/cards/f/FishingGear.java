package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIC", collectorNumber = "461")
public class FishingGear extends Card {

    public FishingGear() {
        CreateTokenEffect fish = new CreateTokenEffect("Fish", 1, 1, CardColor.BLUE,
                List.of(CardSubtype.FISH), Set.of(), Set.of());
        addTrigger(Triggers.equippedCreatureDealsCombatDamageToPlayer(),
                SequenceEffect.of(
                        ExileTopCardOfOwnLibraryEffect.of(Subjects.triggeringPlayer()),
                        new ConditionalReplacementEffect(
                                HappenedThisWay.toACard(StepKind.EXILE, new CardIsPermanentPredicate()),
                                fish,
                                new MayEffect(
                                        ReturnAllCardsExiledWithSourceEffect.putOntoBattlefield(
                                                Subjects.cardsThisWay(StepKind.EXILE)),
                                        "Put the exiled card onto the battlefield under your control?",
                                        fish))));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
