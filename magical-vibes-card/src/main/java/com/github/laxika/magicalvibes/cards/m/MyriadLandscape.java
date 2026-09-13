package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForUpToTwoBasicLandsSharingTypeEffect;

import java.util.List;

@CardRegistration(set = "A25", collectorNumber = "243")
public class MyriadLandscape extends Card {

    public MyriadLandscape() {
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {2}, {T}, Sacrifice Myriad Landscape: Search your library for up to two basic land cards
        // that share a land type, put them onto the battlefield tapped, then shuffle.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new SacrificeSelfCost(), new SearchLibraryForUpToTwoBasicLandsSharingTypeEffect()),
                "{2}, {T}, Sacrifice Myriad Landscape: Search your library for up to two basic land cards "
                        + "that share a land type, put them onto the battlefield tapped, then shuffle."
        ));
    }
}
