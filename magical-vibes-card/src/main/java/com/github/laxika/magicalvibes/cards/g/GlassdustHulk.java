package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;

import java.util.List;

@CardRegistration(set = "ARB", collectorNumber = "7")
public class GlassdustHulk extends Card {

    public GlassdustHulk() {
        // Whenever another artifact you control enters, this creature gets +1/+1 until end of turn
        // and can't be blocked this turn. Both effects are self-scoped (sourcePermanentId).
        addEffect(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD, new BoostSelfEffect(1, 1));
        addEffect(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD, new MakeCreatureUnblockableEffect(true));

        // Cycling {W/U} ({W/U}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addHandActivatedAbility(new ActivatedAbility(false, "{W/U}",
                List.of(new DrawCardEffect(1)),
                "Cycling {W/U} ({W/U}, Discard this card: Draw a card.)"));
    }
}
