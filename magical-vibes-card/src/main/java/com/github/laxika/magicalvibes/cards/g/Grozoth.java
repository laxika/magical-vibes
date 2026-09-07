package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "53")
public class Grozoth extends Card {

    public Grozoth() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchLibraryEffect(
                        new Fixed(Integer.MAX_VALUE),
                        null,
                        LibrarySearchDestination.HAND,
                        new ManaValueBound(new Fixed(9), true, 0),
                        false
                ),
                "Search your library for any number of cards that have mana value 9?"
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(new RemoveKeywordEffect(Keyword.DEFENDER, GrantScope.SELF)),
                "{4}: This creature loses defender until end of turn."
        ));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}{U}",
                List.of(new SearchLibraryEffect(null, LibrarySearchDestination.HAND,
                        new ManaValueBound(new LastDiscardedCardManaValue(), true, 0))),
                "Transmute {1}{U}{U} ({1}{U}{U}, Discard this card: Search your library for a card with the same mana value as this card, reveal it, put it into your hand, then shuffle. Transmute only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
