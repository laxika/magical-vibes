package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfSourceCardIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringRoomDoorConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "YDSK", collectorNumber = "4")
public class SolitaryStudyEndlessCorridor extends Card {

    public SolitaryStudyEndlessCorridor() {
        setRoomDoorManaCosts(List.of("{1}{W}", "{1}{W}"));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Solitary Study", List.of())
                        .withManaCost("{1}{W}"),
                new ChooseOneEffect.ChooseOneOption("Endless Corridor", List.of())
                        .withManaCost("{1}{W}")
        )));

        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 0, GrantScope.ALL_OWN_CREATURES));

        var creatureYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate()));
        GrantKeywordEffect firstStrike = new GrantKeywordEffect(
                Keyword.FIRST_STRIKE, GrantScope.TARGET, creatureYouControl);
        addEffect(EffectSlot.ON_SELF_ROOM_DOOR_UNLOCKED,
                new TriggeringRoomDoorConditionalEffect(1,
                        SequenceEffect.of(
                                new ConjureDuplicateOfSourceCardIntoHandEffect("YDSK", "4"),
                                new QueueReflexiveAbilityEffect(firstStrike))));
    }
}
