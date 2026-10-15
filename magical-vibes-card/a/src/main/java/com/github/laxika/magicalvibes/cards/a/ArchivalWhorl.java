package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
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
        addEffect(EffectSlot.STATIC, new GiftEffect());
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new GiftPromised(),
                ConjureCardToOpponentBattlefieldEffect.gift("WOT", "25")));
        addEffect(EffectSlot.SPELL, new ShuffleControllerHandAndGraveyardIntoLibraryEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect(7));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new NotCondition(new GiftPromised()),
                new EachOtherPlayerShufflesHandAndGraveyardIntoLibraryEffect()));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new NotCondition(new GiftPromised()),
                new EachOtherPlayerDrawsCardEffect(7)));
    }
}
