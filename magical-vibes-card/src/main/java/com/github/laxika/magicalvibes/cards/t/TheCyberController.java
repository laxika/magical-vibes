package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MillEachOpponentAndPutMilledCreaturesFaceDownAsCybermenEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "WHO", collectorNumber = "119")
public class TheCyberController extends Card {

    public TheCyberController() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MillEachOpponentAndPutMilledCreaturesFaceDownAsCybermenEffect(new XValue()));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentIsArtifactPredicate()));
    }
}
