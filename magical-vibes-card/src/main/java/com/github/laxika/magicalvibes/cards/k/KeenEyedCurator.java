package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ExiledCardTypeThreshold;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "181")
public class KeenEyedCurator extends Card {

    public KeenEyedCurator() {
        ExiledCardTypeThreshold threshold = new ExiledCardTypeThreshold(4);
        addEffect(EffectSlot.STATIC, new ConditionalEffect(threshold, new BoostSelfEffect(4, 4)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(threshold,
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(1, null, true)),
                "{1}: Exile target card from a graveyard."
        ));
    }
}
