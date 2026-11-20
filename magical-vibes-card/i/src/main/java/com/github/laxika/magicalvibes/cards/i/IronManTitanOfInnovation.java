package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1731")
@CardRegistration(set = "MAR", collectorNumber = "91")
public class IronManTitanOfInnovation extends Card {

    public IronManTitanOfInnovation() {
        addTrigger(Triggers.attacks(), SequenceEffect.of(
                CreateTokenEffect.ofTreasureToken(1),
                new MayEffect(
                        SequenceEffect.of(
                                new SacrificePermanentsEffect(1, new PermanentAllOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentNotPredicate(new PermanentIsCreaturePredicate())
                                )), Subjects.you()),
                                new ConditionalEffect(HappenedThisWay.sacrificed(), new SearchLibraryEffect(
                                        new CardTypePredicate(CardType.ARTIFACT),
                                        LibrarySearchDestination.BATTLEFIELD_TAPPED,
                                        new ManaValueBound(AmountThisWay.sacrificedManaValue(), true, 1)))),
                        "Sacrifice a noncreature artifact?")));
    }
}
