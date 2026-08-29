package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "TOR", collectorNumber = "122")
public class CentaurChieftain extends Card {

    public CentaurChieftain() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new GraveyardCardThreshold(7, null),
                new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_ENTER_BATTLEFIELD,
                        SequenceEffect.of(
                                new BoostAllOwnCreaturesEffect(1, 1),
                                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.ALL_OWN_CREATURES)),
                        GrantScope.SELF)));
    }
}
