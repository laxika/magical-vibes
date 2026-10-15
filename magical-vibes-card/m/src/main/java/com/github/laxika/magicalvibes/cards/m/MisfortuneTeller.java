package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardWithConditionalEffectsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NCC", collectorNumber = "38")
@CardRegistration(set = "NCC", collectorNumber = "139")
@CardRegistration(set = "OTC", collectorNumber = "141")
public class MisfortuneTeller extends Card {

    public MisfortuneTeller() {
        var rogueToken = new CreateTokenEffect(
                "Rogue", 2, 2, CardColor.BLACK, List.of(CardSubtype.ROGUE), Set.of(), Set.of());
        var exileEffect = new ExileTargetCardFromGraveyardWithConditionalEffectsEffect(
                new CardTypePredicate(CardType.CREATURE),
                rogueToken,
                new CardTypePredicate(CardType.LAND),
                CreateTokenEffect.ofTreasureToken(1),
                new GainLifeEffect(3));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, exileEffect);
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, exileEffect);
    }
}
