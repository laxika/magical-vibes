package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.CantBeSacrificedEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.NextPlayerGainsControlOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerDirection;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import java.util.List;

@CardRegistration(set = "LTC", collectorNumber = "76")
@CardRegistration(set = "LTC", collectorNumber = "156")
public class HithlainRope extends Card {

    public HithlainRope() {
        addEffect(EffectSlot.STATIC, new CantBeSacrificedEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new SearchLibraryEffect(
                                CardPredicateUtils.basicLand(),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED),
                        new NextPlayerGainsControlOfSourceEffect(PlayerDirection.RIGHT)),
                "{1}, {T}: Search your library for a basic land card, put it onto the battlefield tapped, "
                        + "then shuffle. The player to your right gains control of this artifact."));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new DrawCardEffect(),
                        new NextPlayerGainsControlOfSourceEffect(PlayerDirection.RIGHT)),
                "{2}, {T}: Draw a card. The player to your right gains control of this artifact."));
    }
}
