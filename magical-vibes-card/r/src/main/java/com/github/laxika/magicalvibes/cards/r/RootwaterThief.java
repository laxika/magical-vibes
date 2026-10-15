package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetLibraryEffect;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "40")
public class RootwaterThief extends Card {

    public RootwaterThief() {
        addActivatedAbility(new ActivatedAbility(false, "{U}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "{U}: This creature gains flying until end of turn."));

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new MayPayManaEffect("{2}",
                new SearchTargetLibraryEffect(1, null, LibrarySearchDestination.EXILE, false),
                "Pay {2} to search that player's library for a card and exile it?"));
    }
}
