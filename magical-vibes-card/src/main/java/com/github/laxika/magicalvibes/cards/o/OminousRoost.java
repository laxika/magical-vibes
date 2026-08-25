package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CastFromGraveyardTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MID", collectorNumber = "65")
public class OminousRoost extends Card {

    public OminousRoost() {
        CreateTokenEffect birdToken = new CreateTokenEffect(
                1, "Bird", 1, 1, CardColor.BLUE, List.of(CardSubtype.BIRD),
                Set.of(Keyword.FLYING), Set.of(),
                Map.of(EffectSlot.STATIC, new CanBlockOnlyIfAttackerMatchesPredicateEffect(
                        new PermanentHasKeywordPredicate(Keyword.FLYING), "creatures with flying")));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, birdToken);
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new CastFromGraveyardTriggerEffect(List.of(birdToken)));
    }
}
