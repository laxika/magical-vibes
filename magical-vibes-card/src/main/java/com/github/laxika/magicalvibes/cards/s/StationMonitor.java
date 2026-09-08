package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "EOE", collectorNumber = "230")
public class StationMonitor extends Card {

    private static final CreateTokenEffect DRONE = new CreateTokenEffect(
            1,
            "Drone",
            1,
            1,
            null,
            List.of(CardSubtype.DRONE),
            Set.of(Keyword.FLYING),
            Set.of(CardType.ARTIFACT),
            Map.of(EffectSlot.STATIC, new CanBlockOnlyIfAttackerMatchesPredicateEffect(
                    new PermanentHasKeywordPredicate(Keyword.FLYING),
                    "creatures with flying")));

    public StationMonitor() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, SpellCastTriggerEffect.nth(
                2,
                null,
                List.of(DRONE)));
    }
}
