package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CSP", collectorNumber = "24")
public class WhiteShieldCrusader extends Card {

    public WhiteShieldCrusader() {
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.BLACK)));
        addActivatedAbility(new ActivatedAbility(false, "{W}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "{W}: This creature gains flying until end of turn."));
        addActivatedAbility(new ActivatedAbility(false, "{W}{W}",
                List.of(new BoostSelfEffect(1, 0)),
                "{W}{W}: This creature gets +1/+0 until end of turn."));
    }
}
