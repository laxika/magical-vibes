package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "EOE", collectorNumber = "223")
public class PinnacleEmissary extends Card {

    public PinnacleEmissary() {
        CreateTokenEffect drone = new CreateTokenEffect(
                1,
                "Drone",
                1,
                1,
                null,
                List.of(),
                Set.of(Keyword.FLYING),
                Set.of(CardType.ARTIFACT),
                Map.of(EffectSlot.STATIC, new CanBlockOnlyIfAttackerMatchesPredicateEffect(
                        new PermanentHasKeywordPredicate(Keyword.FLYING),
                        "creatures with flying")));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(new CardTypePredicate(CardType.ARTIFACT), List.of(drone)));
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{U/R}"))));
    }
}
