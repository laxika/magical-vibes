package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtLeastPredicate;

@CardRegistration(set = "DTK", collectorNumber = "239")
public class GateSmasher extends Card {

    private static final PermanentToughnessAtLeastPredicate TOUGHNESS_FOUR_OR_GREATER =
            new PermanentToughnessAtLeastPredicate(4);

    public GateSmasher() {
        setAttachRestriction(TOUGHNESS_FOUR_OR_GREATER);
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, 0, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility(
                "{3}",
                TOUGHNESS_FOUR_OR_GREATER,
                "Gate Smasher can be attached only to a creature with toughness 4 or greater"));
    }
}
