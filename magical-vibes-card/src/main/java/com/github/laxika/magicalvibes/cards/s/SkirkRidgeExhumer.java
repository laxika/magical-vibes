package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "FUT", collectorNumber = "77")
public class SkirkRidgeExhumer extends Card {

    public SkirkRidgeExhumer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(new DiscardCardTypeCost(null, null), festeringGoblinToken()),
                "{B}, {T}, Discard a card: Create a 1/1 black Zombie Goblin creature token named Festering Goblin."
                        + " It has \"When this token dies, target creature gets -1/-1 until end of turn.\""
        ));
    }

    private static CreateTokenEffect festeringGoblinToken() {
        return new CreateTokenEffect(
                1,
                "Festering Goblin",
                1,
                1,
                CardColor.BLACK,
                List.of(CardSubtype.ZOMBIE, CardSubtype.GOBLIN),
                Set.of(),
                Set.of(),
                Map.of(EffectSlot.ON_DEATH, new BoostTargetCreatureEffect(-1, -1))
        );
    }
}
