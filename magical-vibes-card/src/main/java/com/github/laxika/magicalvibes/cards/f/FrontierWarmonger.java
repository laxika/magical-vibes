package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "CMM", collectorNumber = "224")
@CardRegistration(set = "LTC", collectorNumber = "217")
public class FrontierWarmonger extends Card {

    public FrontierWarmonger() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new GrantKeywordEffect(
                Keyword.MENACE, GrantScope.OWN_CREATURES, new PermanentIsAttackingPredicate()));
    }
}
