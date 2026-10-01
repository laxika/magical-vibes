package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SurveilThenEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

import java.util.List;

@CardRegistration(set = "YTDM", collectorNumber = "18")
public class AudaciousKnuckleblade extends Card {

    private static final String NAME = "Audacious Knuckleblade";

    public AudaciousKnuckleblade() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new SeekLibraryEffect(
                        1,
                        new CardNamedPredicate(NAME),
                        LibrarySearchDestination.BATTLEFIELD_TAPPED)),
                "Exhaust — {2}{G}: Seek a card named Audacious Knuckleblade and put it onto the battlefield tapped."
                        + " (Activate each exhaust ability only once.)"
        ).withMaxActivationsPerGame(1).withExhaust());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(SurveilThenEffect.direct(
                        2,
                        new UntapPermanentsEffect(TapUntapScope.SELF))),
                "Exhaust — {1}{U}: Surveil 2, then untap this creature."
                        + " (Activate each exhaust ability only once.)"
        ).withMaxActivationsPerGame(1).withExhaust());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new GrantKeywordEffect(Keyword.HASTE, GrantScope.ALL_OWN_CREATURES)),
                "Exhaust — {R}: Creatures you control gain haste until end of turn."
                        + " (Activate each exhaust ability only once.)"
        ).withMaxActivationsPerGame(1).withExhaust());
    }
}
