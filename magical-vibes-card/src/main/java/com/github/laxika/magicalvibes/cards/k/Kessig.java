package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageByCreaturesExceptEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "OPC2", collectorNumber = "21")
public class Kessig extends Card {

    public Kessig() {
        addEffect(EffectSlot.STATIC, new PreventAllCombatDamageByCreaturesExceptEffect(
                new PermanentHasSubtypePredicate(CardSubtype.WEREWOLF)));

        addEffect(EffectSlot.CHAOS_TRIGGERED, new BoostAllOwnCreaturesEffect(2, 2));
        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES));
        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new GrantSubtypeUntilEndOfTurnEffect(CardSubtype.WEREWOLF, GrantScope.OWN_CREATURES));
    }
}
