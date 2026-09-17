package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "HOC", collectorNumber = "116")
@CardRegistration(set = "HOC", collectorNumber = "201")
public class OrcishSiegemaster extends Card {

    public OrcishSiegemaster() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.TRAMPLE,
                GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.ORC, CardSubtype.GOBLIN))));
        addEffect(EffectSlot.ON_ATTACK,
                new BoostSelfEffect(new GreatestPowerAmongControlled(), new Fixed(0)));
    }
}
