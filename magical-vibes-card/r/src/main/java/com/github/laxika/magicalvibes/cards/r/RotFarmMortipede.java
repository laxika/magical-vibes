package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MKM", collectorNumber = "102")
public class RotFarmMortipede extends Card {

    public RotFarmMortipede() {
        addEffect(EffectSlot.ON_CONTROLLER_CREATURE_CARDS_LEAVE_GRAVEYARD,
                SequenceEffect.of(
                        new BoostSelfEffect(1, 0),
                        new GrantKeywordEffect(Keyword.MENACE, GrantScope.SELF),
                        new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF)));
    }
}
