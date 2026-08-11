package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "226")
public class GolosTirelessPilgrim extends Card {

    public GolosTirelessPilgrim() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(
                        new SearchLibraryEffect(new CardTypePredicate(CardType.LAND), LibrarySearchDestination.BATTLEFIELD_TAPPED),
                        "Search your library for a land card?"));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}{U}{B}{R}{G}",
                List.of(new ExileTopCardMayPlayThisTurnEffect(3, true)),
                "{2}{W}{U}{B}{R}{G}: Exile the top three cards of your library. You may play them this turn without paying their mana costs."));
    }
}
