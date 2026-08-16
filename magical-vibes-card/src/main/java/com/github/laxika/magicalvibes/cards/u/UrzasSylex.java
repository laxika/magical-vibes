package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesLandsThenDestroyRestEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SelfExiledFromBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "40")
public class UrzasSylex extends Card {

    public UrzasSylex() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{W}{W}",
                List.of(new ExileSelfCost(), new EachPlayerChoosesLandsThenDestroyRestEffect(6)),
                "{2}{W}{W}, {T}, Exile Urza's Sylex: Each player chooses six lands they control. "
                        + "Destroy all other permanents. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new SelfExiledFromBattlefieldEffect(new MayPayManaEffect(
                        "{2}",
                        new SearchLibraryEffect(new CardTypePredicate(CardType.PLANESWALKER)),
                        "Pay {2} to search your library for a planeswalker card?")));
    }
}
