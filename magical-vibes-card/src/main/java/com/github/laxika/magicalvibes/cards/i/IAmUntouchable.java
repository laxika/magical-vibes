package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSC", collectorNumber = "336")
public class IAmUntouchable extends Card {

    public IAmUntouchable() {
        // You and permanents you control have hexproof.
        addEffect(EffectSlot.STATIC, new GrantControllerKeywordEffect(Keyword.HEXPROOF));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.OWN_PERMANENTS));

        // When combat damage is dealt to you, create a 4/4 colorless Scarecrow artifact creature
        // token with vigilance, then abandon this scheme.
        addEffect(EffectSlot.ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU, SequenceEffect.of(
                new CreateTokenEffect("Scarecrow", 4, 4, null,
                        List.of(CardSubtype.SCARECROW), Set.of(Keyword.VIGILANCE), Set.of(CardType.ARTIFACT)),
                new SacrificeSelfEffect()));
    }
}
