package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "252")
public class WortBoggartAuntie extends Card {

    public WortBoggartAuntie() {
        // Fear is auto-loaded from Scryfall.
        // At the beginning of your upkeep, you may return target Goblin card from your graveyard to your hand.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardSubtypePredicate(CardSubtype.GOBLIN))
                .targetGraveyard(true)
                .build(), "Return the targeted Goblin card to your hand?"));
    }
}
