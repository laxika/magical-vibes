package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "129")
public class FrostFairLureFish extends Card {

    public FrostFairLureFish() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                2, "Fish", 1, 1, CardColor.BLUE, List.of(CardSubtype.FISH), false));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofTreasureToken(2, true));

        PermanentHasSubtypePredicate fish = new PermanentHasSubtypePredicate(CardSubtype.FISH);
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HASTE, GrantScope.ALL_OWN_CREATURES, fish));
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new CantBeBlockedByCreaturesMatchingPredicateEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.HUMAN)),
                GrantScope.ALL_OWN_CREATURES, fish));

        addCastingOption(new ForetellCast("{3}{U}{R}"));
    }
}
