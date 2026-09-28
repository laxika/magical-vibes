package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "82")
public class AncientGoldDragon extends Card {

    public AncientGoldDragon() {
        CreateTokenEffect faerieDragon = new CreateTokenEffect(
                new EventValue(), "Faerie Dragon", 1, 1, CardColor.BLUE,
                List.of(CardSubtype.FAERIE, CardSubtype.DRAGON), Set.of(Keyword.FLYING), Set.of());
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new RollD20Effect(faerieDragon, faerieDragon));
    }
}
