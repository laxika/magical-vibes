package com.github.laxika.magicalvibes.cards.o;

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

@CardRegistration(set = "FEM", collectorNumber = "42a")
@CardRegistration(set = "FEM", collectorNumber = "42b")
@CardRegistration(set = "FEM", collectorNumber = "42c")
@CardRegistration(set = "FEM", collectorNumber = "137")
@CardRegistration(set = "FEM", collectorNumber = "139")
public class OrderOfTheEbonHand extends Card {

    public OrderOfTheEbonHand() {
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.WHITE)));

        addActivatedAbility(new ActivatedAbility(false, "{B}",
                List.of(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)),
                "{B}: This creature gains first strike until end of turn."));
        addActivatedAbility(new ActivatedAbility(false, "{B}{B}", List.of(new BoostSelfEffect(1, 0)),
                "{B}{B}: This creature gets +1/+0 until end of turn."));
    }
}
