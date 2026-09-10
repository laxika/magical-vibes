package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceRoomDoorUnlocked;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringRoomDoorConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "227")
public class RestrictedOfficeLectureHall extends Card {

    public RestrictedOfficeLectureHall() {
        setRoomDoorManaCosts(List.of("{2}{W}{W}", "{5}{U}{U}"));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Restricted Office", List.of())
                        .withManaCost("{2}{W}{W}"),
                new ChooseOneEffect.ChooseOneOption("Lecture Hall", List.of())
                        .withManaCost("{5}{U}{U}")
        )));

        addEffect(EffectSlot.ON_SELF_ROOM_DOOR_UNLOCKED,
                new TriggeringRoomDoorConditionalEffect(0,
                        new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentPowerAtLeastPredicate(3)
                        )))));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceRoomDoorUnlocked(1),
                new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.OWN_PERMANENTS,
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()))));
    }
}
