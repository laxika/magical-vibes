package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentCreatesTokenUnlessSacrificesCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "120")
@CardRegistration(set = "WHO", collectorNumber = "406")
@CardRegistration(set = "WHO", collectorNumber = "725")
@CardRegistration(set = "WHO", collectorNumber = "997")
public class TheDalekEmperor extends Card {

    public TheDalekEmperor() {
        // Affinity for Daleks.
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.DALEK), CountScope.CONTROLLER)));

        // Other Daleks you control have haste.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HASTE, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.DALEK)));

        // Each opponent sacrifices a creature or the controller creates a Dalek.
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new EachOpponentCreatesTokenUnlessSacrificesCreatureEffect(new CreateTokenEffect(
                        "Dalek", 3, 3, CardColor.BLACK,
                        List.of(CardSubtype.DALEK), Set.of(Keyword.MENACE),
                        Set.of(CardType.ARTIFACT))));
    }
}
