package com.github.laxika.magicalvibes.cards.s;

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

@CardRegistration(set = "EVE", collectorNumber = "95")
public class StillmoonCavalier extends Card {

    public StillmoonCavalier() {
        // Protection from white and from black.
        addEffect(EffectSlot.STATIC,
                new ProtectionFromColorsEffect(Set.of(CardColor.WHITE, CardColor.BLACK)));

        // {W/B}: This creature gains flying until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{W/B}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "{W/B}: This creature gains flying until end of turn."));

        // {W/B}: This creature gains first strike until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{W/B}",
                List.of(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)),
                "{W/B}: This creature gains first strike until end of turn."));

        // {W/B}{W/B}: This creature gets +1/+0 until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{W/B}{W/B}",
                List.of(new BoostSelfEffect(1, 0)),
                "{W/B}{W/B}: This creature gets +1/+0 until end of turn."));
    }
}
