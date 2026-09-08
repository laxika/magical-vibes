package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnCreatedPermanentsEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "68")
public class RetrieveTheEsper extends Card {

    public RetrieveTheEsper() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Robot Warrior", 3, 3, CardColor.BLUE,
                List.of(CardSubtype.ROBOT, CardSubtype.WARRIOR), Set.of(), Set.of(CardType.ARTIFACT)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new CastFromZone(Zone.GRAVEYARD),
                new PutCountersOnCreatedPermanentsEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2))));
        addCastingOption(new FlashbackCast("{5}{U}"));
    }
}
