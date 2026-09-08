package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "25")
public class MagitekInfantry extends Card {

    public MagitekInfantry() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsAnotherPermanent(new PermanentIsArtifactPredicate()),
                new StaticBoostEffect(1, 0, GrantScope.SELF)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(new SearchLibraryEffect(
                        new CardNamedPredicate("Magitek Infantry"), LibrarySearchDestination.BATTLEFIELD_TAPPED)),
                "{2}{W}: Search your library for a card named Magitek Infantry, put it onto the battlefield tapped, then shuffle."
        ));
    }
}
