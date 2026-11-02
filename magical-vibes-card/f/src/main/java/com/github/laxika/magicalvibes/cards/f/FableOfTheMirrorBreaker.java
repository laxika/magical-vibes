package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.ReflectionOfKikiJiki;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUpToThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "141")
public class FableOfTheMirrorBreaker extends Card {

    public FableOfTheMirrorBreaker() {
        setBackFaceCard(new ReflectionOfKikiJiki());

        addTrigger(Triggers.chapter(1), new CreateTokenEffect(
                1, "Goblin Shaman", 2, 2, CardColor.RED,
                List.of(CardSubtype.GOBLIN, CardSubtype.SHAMAN), Set.of(), Set.of(),
                List.of(new TriggeredAbility(Triggers.attacks(), CreateTokenEffect.ofTreasureToken(1)))));
        addTrigger(Triggers.chapter(2), new DiscardUpToThenDrawThatManyEffect(2));
        addTrigger(Triggers.chapter(3), new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "ReflectionOfKikiJiki";
    }
}
