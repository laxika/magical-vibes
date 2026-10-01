package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchPlayer;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringSpellWithSuspendCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "WHO", collectorNumber = "153")
@CardRegistration(set = "WHO", collectorNumber = "437")
@CardRegistration(set = "WHO", collectorNumber = "758")
@CardRegistration(set = "WHO", collectorNumber = "1028")
public class RoryWilliams extends Card {

    private static final String PARTNER_NAME = "Amy Pond";

    public RoryWilliams() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchTargetLibraryEffect(
                        1,
                        new CardNamedPredicate(PARTNER_NAME),
                        LibrarySearchDestination.HAND,
                        true,
                        LibrarySearchPlayer.TARGET_PLAYER),
                "Have target player put " + PARTNER_NAME + " into their hand from their library?",
                null,
                MayChoicePlayer.TARGET_PLAYER
        ));

        addEffect(EffectSlot.ON_SELF_CAST,
                ExileTriggeringSpellWithSuspendCountersEffect.unlessCastFromExile(
                        3, CreateTokenEffect.ofClueToken(1)));
    }
}
