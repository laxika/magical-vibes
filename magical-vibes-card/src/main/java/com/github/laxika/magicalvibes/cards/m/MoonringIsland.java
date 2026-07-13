package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "276")
public class MoonringIsland extends Card {

    public MoonringIsland() {
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {U}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLUE)),
                "{T}: Add {U}."
        ));

        // {U}, {T}: Look at the top card of target player's library.
        // Activate only if you control two or more blue permanents.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(new LookAtTopCardOfTargetLibraryEffect(1)),
                "{U}, {T}: Look at the top card of target player's library. "
                        + "Activate only if you control two or more blue permanents.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player")
        ).withRequiredControlledPermanents(
                new PermanentColorInPredicate(Set.of(CardColor.BLUE)), 2, "blue permanents"));
    }
}
