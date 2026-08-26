package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MOM", collectorNumber = "253")
public class SculptedPerfection extends Card {

    public SculptedPerfection() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, incubatorToken());
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.PHYREXIAN)));
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
                false, false, false, 2, Set.of()
        );
    }
}
