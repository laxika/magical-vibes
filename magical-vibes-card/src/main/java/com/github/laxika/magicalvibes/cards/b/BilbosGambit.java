package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GiftPromised;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GiftEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCastSpellsThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetSpellToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentCreatesTokenEffect;

@CardRegistration(set = "HOB", collectorNumber = "5")
public class BilbosGambit extends Card {

    public BilbosGambit() {
        addEffect(EffectSlot.STATIC, new GiftEffect());
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new GiftPromised(),
                TargetOpponentCreatesTokenEffect.gift(CreateTokenEffect.ofTreasureToken(1))));
        addEffect(EffectSlot.SPELL, new ReturnTargetSpellToHandEffect());
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new GiftPromised(),
                new PlayersCantCastSpellsThisTurnEffect()));
    }
}
