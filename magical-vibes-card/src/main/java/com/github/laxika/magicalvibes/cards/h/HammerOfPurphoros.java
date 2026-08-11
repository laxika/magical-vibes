package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THS", collectorNumber = "124")
public class HammerOfPurphoros extends Card {

    public HammerOfPurphoros() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HASTE, GrantScope.OWN_CREATURES));
        addActivatedAbility(new ActivatedAbility(
                true, "{2}{R}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsLandPredicate(), "Sacrifice a land", false),
                        new CreateTokenEffect(
                                "Golem", 3, 3, null, List.of(CardSubtype.GOLEM), Set.of(),
                                Set.of(CardType.ARTIFACT, CardType.ENCHANTMENT))),
                "{2}{R}, {T}, Sacrifice a land: Create a 3/3 colorless Golem enchantment artifact creature token."));
    }
}
