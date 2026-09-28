package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GiftPromised;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GiftEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YBLB", collectorNumber = "6")
public class PoolResources extends Card {

    public PoolResources() {
        addEffect(EffectSlot.STATIC, new GiftEffect());
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new GiftPromised(),
                TargetOpponentCreatesTokenEffect.gift(fishToken())));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new NotCondition(new GiftPromised()),
                new DrawCardEffect(2)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new GiftPromised(),
                new SeekLibraryEffect(2, new CardNotPredicate(new CardTypePredicate(CardType.LAND)))));
    }

    private static CreateTokenEffect fishToken() {
        return new CreateTokenEffect(1, "Fish", 1, 1, CardColor.BLUE, List.of(CardSubtype.FISH),
                Set.of(), Set.of(), true);
    }
}
