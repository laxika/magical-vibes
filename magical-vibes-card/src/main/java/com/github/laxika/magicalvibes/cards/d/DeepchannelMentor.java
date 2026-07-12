package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "35")
public class DeepchannelMentor extends Card {

    public DeepchannelMentor() {
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new CantBeBlockedEffect(),
                GrantScope.ALL_OWN_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.BLUE))
        ));
    }
}
