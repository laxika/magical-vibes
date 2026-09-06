package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "173")
public class TolariaWest extends Card {

    public TolariaWest() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}{U}",
                List.of(new SearchLibraryEffect(null, LibrarySearchDestination.HAND,
                        new ManaValueBound(new LastDiscardedCardManaValue(), true, 0))),
                "Transmute {1}{U}{U} ({1}{U}{U}, Discard this card: Search your library for a card with mana value 0, reveal it, put it into your hand, then shuffle. Transmute only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
