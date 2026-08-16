package com.github.laxika.magicalvibes.cards.a;

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
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndCreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M21", collectorNumber = "88")
public class ArchfiendsVessel extends Card {

    public ArchfiendsVessel() {
        CreateTokenEffect demon = new CreateTokenEffect("Demon", 5, 5, CardColor.BLACK,
                List.of(CardSubtype.DEMON), Set.of(Keyword.FLYING), Set.of());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new CastFromZone(Zone.GRAVEYARD),
                        new ExileSelfAndCreateTokenEffect(demon)));
        addEffect(EffectSlot.ON_SELF_ENTERS_FROM_GRAVEYARD,
                new ExileSelfAndCreateTokenEffect(demon));
    }
}
