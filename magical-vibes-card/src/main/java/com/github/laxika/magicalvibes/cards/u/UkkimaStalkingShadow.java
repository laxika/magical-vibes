package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetPlayerLibraryForNamedCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "OTC", collectorNumber = "247")
public class UkkimaStalkingShadow extends Card {

    private static final String PARTNER_NAME = "Cazur, Ruthless Stalker";

    public UkkimaStalkingShadow() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchTargetPlayerLibraryForNamedCardToHandEffect(PARTNER_NAME),
                "Have target player put " + PARTNER_NAME + " into their hand from their library?",
                null,
                MayChoicePlayer.TARGET_PLAYER));

        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, SequenceEffect.of(
                new DealDamageToTargetPlayerOrPlaneswalkerEffect(new SourcePower()),
                new GainLifeEffect(new SourcePower())));
    }
}
