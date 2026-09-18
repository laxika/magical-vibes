package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RevealSubtypeOrEntersTappedEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "C14", collectorNumber = "60")
@CardRegistration(set = "ECC", collectorNumber = "149")
public class FlamekinVillage extends Card {

    public FlamekinVillage() {
        // As this land enters, you may reveal an Elemental card from your hand.
        // If you don't, this land enters tapped.
        addEffect(EffectSlot.STATIC, new RevealSubtypeOrEntersTappedEffect(CardSubtype.ELEMENTAL));

        // {T}: Add {R}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));

        // {R}, {T}: Target creature gains haste until end of turn.
        addActivatedAbility(new ActivatedAbility(true, "{R}",
                List.of(new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)),
                "{R}, {T}: Target creature gains haste until end of turn.",
                TargetFilters.creature()));
    }
}
