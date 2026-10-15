package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GiftPromised;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GiftEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "52")
public class IntoTheFloodMaw extends Card {

    public IntoTheFloodMaw() {
        addEffect(EffectSlot.STATIC, new GiftEffect());
        target(TargetFilters.withGift(
                TargetFilters.creatureAnOpponentControls(),
                TargetFilters.nonlandPermanentAnOpponentControls()))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(new GiftPromised(),
                        TargetOpponentCreatesTokenEffect.gift(fishToken())))
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
    }

    private static CreateTokenEffect fishToken() {
        return new CreateTokenEffect(1, "Fish", 1, 1, CardColor.BLUE, List.of(CardSubtype.FISH),
                Set.of(), Set.of(), true);
    }
}
