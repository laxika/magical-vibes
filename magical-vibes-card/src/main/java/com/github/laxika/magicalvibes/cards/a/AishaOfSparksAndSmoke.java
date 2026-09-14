package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SLX", collectorNumber = "12")
public class AishaOfSparksAndSmoke extends Card {

    public AishaOfSparksAndSmoke() {
        addActivatedAbility(new ActivatedAbility(false, "{R/W}",
                List.of(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)),
                "{R/W}: Aisha of Sparks and Smoke gains first strike until end of turn."));

        addEffect(EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE,
                new MayCastAnySpellFromHandWithoutPayingManaCostEffect(
                        new CardTypePredicate(CardType.SORCERY), new EventValue()));
    }
}
