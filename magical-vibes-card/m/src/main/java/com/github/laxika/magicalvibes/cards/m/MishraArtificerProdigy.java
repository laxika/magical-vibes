package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchSameNameCardToBattlefieldOnArtifactSpellCastEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSP", collectorNumber = "243")
public class MishraArtificerProdigy extends Card {

    public MishraArtificerProdigy() {
        addTrigger(Triggers.youCastSpell(), new MayEffect(
                new SearchSameNameCardToBattlefieldOnArtifactSpellCastEffect(),
                "Search your graveyard, hand, and/or library for a card with the same name as that spell and put it onto the battlefield?"));
    }
}
