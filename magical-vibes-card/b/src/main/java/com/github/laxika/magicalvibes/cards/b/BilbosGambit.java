package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
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
        addStaticEffect(new GiftEffect());
        addSpellEffect(new ConditionalEffect(new GiftPromised(),
                TargetOpponentCreatesTokenEffect.gift(CreateTokenEffect.ofTreasureToken(1))));
        addSpellEffect(new ReturnTargetSpellToHandEffect());
        addSpellEffect(new ConditionalEffect(new GiftPromised(),
                new PlayersCantCastSpellsThisTurnEffect()));
    }
}
