package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.NthAbilityResolutionThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "SNC", collectorNumber = "161")
public class VenomConnoisseur extends Card {

    public VenomConnoisseur() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                SequenceEffect.of(
                        new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF),
                        new ConditionalEffect(
                                new NthAbilityResolutionThisTurn(2),
                                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.ALL_OWN_CREATURES))));
    }
}
