package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;

@CardRegistration(set = "AFC", collectorNumber = "129")
@CardRegistration(set = "C19", collectorNumber = "27")
public class IgniteTheFuture extends Card {

    public IgniteTheFuture() {
        addSpellEffect(new ConditionalReplacementEffect(
                new CastFromZone(Zone.GRAVEYARD),
                new ExileTopCardMayPlayThisTurnEffect(3, false).until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN),
                new ExileTopCardMayPlayThisTurnEffect(3, true).until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN)));
        addCastingOption(new FlashbackCast("{7}{R}"));
    }
}
