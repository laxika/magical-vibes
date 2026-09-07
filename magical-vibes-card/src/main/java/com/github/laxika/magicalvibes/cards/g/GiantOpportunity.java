package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "159")
public class GiantOpportunity extends Card {

    public GiantOpportunity() {
        addEffect(EffectSlot.SPELL, new SacrificePermanentsOrElseEffect(
                new PermanentHasSubtypePredicate(CardSubtype.FOOD),
                2,
                giantToken(),
                foodTokens(),
                "Foods"
        ));
    }

    private static CreateTokenEffect giantToken() {
        return new CreateTokenEffect("Giant", 7, 7, CardColor.GREEN, List.of(CardSubtype.GIANT),
                Set.of(), Set.of());
    }

    private static CreateTokenEffect foodTokens() {
        return CreateTokenEffect.ofArtifactToken(3, "Food", List.of(CardSubtype.FOOD), List.of(
                new ActivatedAbility(
                        true,
                        "{2}",
                        List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                        "{2}, {T}, Sacrifice this token: You gain 3 life."
                )
        ));
    }
}
