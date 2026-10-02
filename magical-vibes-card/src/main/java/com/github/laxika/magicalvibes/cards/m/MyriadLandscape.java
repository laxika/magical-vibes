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
@CardRegistration(set = "C14", collectorNumber = "61")
@CardRegistration(set = "CMM", collectorNumber = "421")
@CardRegistration(set = "CMM", collectorNumber = "660")
@CardRegistration(set = "WHO", collectorNumber = "290")
@CardRegistration(set = "WHO", collectorNumber = "881")
@CardRegistration(set = "PIP", collectorNumber = "274")
@CardRegistration(set = "PIP", collectorNumber = "802")
@CardRegistration(set = "C21", collectorNumber = "304")
@CardRegistration(set = "40K", collectorNumber = "285")
@CardRegistration(set = "DSC", collectorNumber = "289")
@CardRegistration(set = "M3C", collectorNumber = "358")
@CardRegistration(set = "MKC", collectorNumber = "276")
@CardRegistration(set = "LCC", collectorNumber = "343")
@CardRegistration(set = "C20", collectorNumber = "292")
@CardRegistration(set = "MIC", collectorNumber = "177")
@CardRegistration(set = "C19", collectorNumber = "261")
@CardRegistration(set = "ONC", collectorNumber = "159")
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
