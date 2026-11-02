package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerGainsControlOfOwnedCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GRN", collectorNumber = "208")
@CardRegistration(set = "SCD", collectorNumber = "253")
public class TrostaniDiscordant extends Card {

    public TrostaniDiscordant() {
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CreateTokenEffect(2, "Soldier", 1, 1,
                CardColor.WHITE, List.of(CardSubtype.SOLDIER), Set.of(Keyword.LIFELINK), Set.of()));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new EachPlayerGainsControlOfOwnedCreaturesEffect());
    }
}
