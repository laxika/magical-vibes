package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "27")
@CardRegistration(set = "2XM", collectorNumber = "41")
public class ArcumDagsson extends Card {

    public ArcumDagsson() {
        // The search doesn't depend on the sacrifice: "that player" is the target's controller, as it is or last was.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(SequenceEffect.of(
                        new SacrificeSelfEffect(Subjects.target(), Subjects.controllerOf(Subjects.target()),
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(), new PermanentIsCreaturePredicate()))),
                        new MayEffect(new SearchLibraryEffect(
                                new CardAllOfPredicate(List.of(
                                        new CardTypePredicate(CardType.ARTIFACT),
                                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)))),
                                LibrarySearchDestination.BATTLEFIELD)
                                .searchedBy(Subjects.controllerOf(Subjects.target())),
                                "Search your library for a noncreature artifact card?", null,
                                MayChoicePlayer.TARGET_PERMANENT_CONTROLLER))),
                "{T}: Target artifact creature's controller sacrifices it. That player may search their library for a noncreature artifact card, put it onto the battlefield, then shuffle."
        ));
    }
}
