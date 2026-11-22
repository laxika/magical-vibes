package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantCardCharacteristicsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YECL", collectorNumber = "23")
public class PepRaucousRaider extends Card {

    public PepRaucousRaider() {
        ActivatedAbility artifactManaAbility = new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false),
                        new AwardAnyColorManaEffect(3)
                ),
                "{T}, Sacrifice an artifact: Add three mana of any one color.");

        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                SequenceEffect.of(
                        new ExileTopCardMayPlayThisTurnEffect(1, false).fromLibraryOf(Subjects.triggeringPlayer()),
                        new PerpetuallyGrantCardCharacteristicsEffect(Set.of(CardType.ARTIFACT), Set.of(), List.of())
                                .to(Subjects.cardsThisWay(StepKind.EXILE, new CardAllOfPredicate(List.of(
                                        new CardIsPermanentPredicate(),
                                        new CardNotPredicate(new CardTypePredicate(CardType.LAND))))))));
        addActivatedAbility(artifactManaAbility);
    }
}
