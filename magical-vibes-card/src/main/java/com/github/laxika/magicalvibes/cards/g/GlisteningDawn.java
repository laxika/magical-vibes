package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateXTokenWithXCountersEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MOM", collectorNumber = "187")
public class GlisteningDawn extends Card {

    public GlisteningDawn() {
        PermanentCount landsYouControl = new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.SPELL, new CreateXTokenWithXCountersEffect(
                incubatorToken(), landsYouControl, CounterType.PLUS_ONE_PLUS_ONE));
        addEffect(EffectSlot.SPELL, new CreateXTokenWithXCountersEffect(
                incubatorToken(), landsYouControl, CounterType.PLUS_ONE_PLUS_ONE));
    }

    private static CreateTokenEffect incubatorToken() {
        ActivatedAbility transform = new ActivatedAbility(
                false,
                "{2}",
                List.of(new TransformSelfEffect()),
                "{2}: Transform this token."
        );
        return new CreateTokenEffect(
                CardType.ARTIFACT, 1, "Incubator", 0, 0, null, null,
                List.of(), Set.of(), Set.of(), false, false, Map.of(), List.of(transform),
                false, false, false, 0, Set.of()
        );
    }
}
