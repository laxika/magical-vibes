package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "220")
public class PyxisOfPandemonium extends Card {

    public PyxisOfPandemonium() {
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new ExileTopCardsToSourceEffect(1, true, false, LibraryScope.EACH_PLAYER)),
                "{T}: Each player exiles the top card of their library face down."));

        addActivatedAbility(new ActivatedAbility(
                true, "{7}",
                List.of(new SacrificeSelfCost(),
                        new ReturnAllCardsExiledWithSourceEffect(false,
                                new CardIsPermanentPredicate(), true)),
                "{7}, {T}, Sacrifice this artifact: Each player turns face up all cards they own "
                        + "exiled with this artifact, then puts all permanent cards among them onto "
                        + "the battlefield."));
    }
}
