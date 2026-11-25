package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

public class EdgarMarkovsCoffin extends Card {

    public EdgarMarkovsCoffin() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                new CreateTokenEffect(
                        1, "Vampire", 1, 1, CardColor.WHITE,
                        Set.of(CardColor.WHITE, CardColor.BLACK),
                        List.of(CardSubtype.VAMPIRE), Set.of(Keyword.LIFELINK), Set.of()),
                PutCountersEffect.onSelf(CounterType.BLOODLINE),
                new ConditionalEffect(new SourceCounterThreshold(3, CounterType.BLOODLINE), SequenceEffect.of(
                        new RemoveAllCountersEffect(CounterType.BLOODLINE),
                        new TransformSelfEffect()))));
    }
}
