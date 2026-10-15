package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RNA", collectorNumber = "205")
public class SeraphOfTheScales extends Card {

    public SeraphOfTheScales() {
        addActivatedAbility(new ActivatedAbility(false, "{W}",
                List.of(new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF)),
                "{W}: This creature gains vigilance until end of turn."));
        addActivatedAbility(new ActivatedAbility(false, "{B}",
                List.of(new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF)),
                "{B}: This creature gains deathtouch until end of turn."));

        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                2, "Spirit", 1, 1, CardColor.WHITE,
                Set.of(CardColor.WHITE, CardColor.BLACK), List.of(CardSubtype.SPIRIT),
                Set.of(Keyword.FLYING), Set.of()));
    }
}
