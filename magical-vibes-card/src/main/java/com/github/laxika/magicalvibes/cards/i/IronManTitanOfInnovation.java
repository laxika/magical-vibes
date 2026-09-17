package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.SacrificedPermanentManaValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "MAR", collectorNumber = "91")
public class IronManTitanOfInnovation extends Card {

    public IronManTitanOfInnovation() {
        addEffect(EffectSlot.ON_ATTACK, SequenceEffect.of(
                CreateTokenEffect.ofTreasureToken(1),
                new MayEffect(
                        new SacrificePermanentThenEffect(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentNotPredicate(new PermanentIsCreaturePredicate())
                                )),
                                new SearchLibraryEffect(
                                        new CardTypePredicate(CardType.ARTIFACT),
                                        LibrarySearchDestination.BATTLEFIELD_TAPPED,
                                        new ManaValueBound(new SacrificedPermanentManaValue(), true, 1)),
                                "a noncreature artifact", false, false),
                        "Sacrifice a noncreature artifact?")));
    }
}
