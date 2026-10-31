package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GiftPromised;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToOpponentBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOtherPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOtherPlayerShufflesHandAndGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.GiftEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleControllerHandAndGraveyardIntoLibraryEffect;

@CardRegistration(set = "YBLB", collectorNumber = "5")
public class ArchivalWhorl extends Card {

    public ArchivalWhorl() {
        addStaticEffect(new GiftEffect());
        addSpellEffect(new ConditionalEffect(new GiftPromised(),
                ConjureCardToOpponentBattlefieldEffect.gift("WOT", "25")));
        addSpellEffect(new ShuffleControllerHandAndGraveyardIntoLibraryEffect());
        addSpellEffect(new DrawCardEffect(7));
        addSpellEffect(new ConditionalEffect(new NotCondition(new GiftPromised()),
                new EachOtherPlayerShufflesHandAndGraveyardIntoLibraryEffect()));
        addSpellEffect(new ConditionalEffect(new NotCondition(new GiftPromised()),
                new EachOtherPlayerDrawsCardEffect(7)));
    }
}
