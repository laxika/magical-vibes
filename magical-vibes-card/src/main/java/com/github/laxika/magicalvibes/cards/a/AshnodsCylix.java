package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "117")
public class AshnodsCylix extends Card {

    public AshnodsCylix() {
        // {3}, {T}: Target player looks at the top three cards of their library, puts one of them
        // back on top of their library, then exiles the rest.
        addActivatedAbility(new ActivatedAbility(
                true, "{3}",
                List.of(new LookAtTopCardsOfTargetLibraryEffect(3, TargetLibraryAction.KEEP_ONE_ON_TOP_EXILE_REST)),
                "{3}, {T}: Target player looks at the top three cards of their library, puts one of them back on top of their library, then exiles the rest."
        ));
    }
}
