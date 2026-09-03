package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "SNC", collectorNumber = "175")
public class CeremonialGroundbreaker extends Card {

    private static final PermanentHasSubtypePredicate CITIZEN =
            new PermanentHasSubtypePredicate(CardSubtype.CITIZEN);

    public CeremonialGroundbreaker() {
        setAttachRestriction(CITIZEN);
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 1, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility(
                "{1}", CITIZEN,
                "Ceremonial Groundbreaker can be attached only to a Citizen"));
    }
}
