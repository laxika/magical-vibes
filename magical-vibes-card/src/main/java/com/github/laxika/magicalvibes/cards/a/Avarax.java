package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "187")
public class Avarax extends Card {

    public Avarax() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchLibraryEffect(new CardNamedPredicate("Avarax"), LibrarySearchDestination.HAND),
                "Search your library for a card named Avarax?"
        ));
        addActivatedAbility(new ActivatedAbility(false, "{1}{R}", List.of(new BoostSelfEffect(1, 0)),
                "{1}{R}: This creature gets +1/+0 until end of turn."));
    }
}
