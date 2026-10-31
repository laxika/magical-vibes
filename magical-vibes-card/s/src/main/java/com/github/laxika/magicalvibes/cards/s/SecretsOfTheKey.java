package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "MID", collectorNumber = "73")
@CardRegistration(set = "DBL", collectorNumber = "73")
public class SecretsOfTheKey extends Card {

    public SecretsOfTheKey() {
        addSpellEffect(CreateTokenEffect.ofClueToken(1));
        addSpellEffect(new ConditionalEffect(new CastFromZone(Zone.GRAVEYARD),
                CreateTokenEffect.ofClueToken(1)));
        addCastingOption(new FlashbackCast("{3}{U}"));
    }
}
