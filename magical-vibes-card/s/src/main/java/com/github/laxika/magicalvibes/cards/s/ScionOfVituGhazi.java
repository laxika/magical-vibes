package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PopulateEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DGM", collectorNumber = "7")
public class ScionOfVituGhazi extends Card {

    public ScionOfVituGhazi() {
        // When this creature enters, if you cast it from your hand, create a 1/1 white Bird
        // creature token with flying, then populate.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new CastFromZone(Zone.HAND),
                new CreateTokenEffect("Bird", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of())));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new CastFromZone(Zone.HAND),
                new PopulateEffect()));
    }
}
