package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.ReflectionOfKikiJiki;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUpToThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "141")
public class FableOfTheMirrorBreaker extends Card {

    public FableOfTheMirrorBreaker() {
        setBackFaceCard(new ReflectionOfKikiJiki());

        addEffect(EffectSlot.SAGA_CHAPTER_I, new CreateTokenEffect(
                1, "Goblin Shaman", 2, 2, CardColor.RED,
                List.of(CardSubtype.GOBLIN, CardSubtype.SHAMAN), Set.of(), Set.of(),
                Map.of(EffectSlot.ON_ATTACK, CreateTokenEffect.ofTreasureToken(1))));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new DiscardUpToThenDrawThatManyEffect(2));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "ReflectionOfKikiJiki";
    }
}
