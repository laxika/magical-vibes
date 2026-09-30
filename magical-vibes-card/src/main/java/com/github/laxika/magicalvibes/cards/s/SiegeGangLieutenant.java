package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerControlsCommander;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "M3C", collectorNumber = "61")
@CardRegistration(set = "M3C", collectorNumber = "113")
public class SiegeGangLieutenant extends Card {

    public SiegeGangLieutenant() {
        // Lieutenant — At the beginning of combat on your turn, if you control your commander,
        // create two 1/1 red Goblin creature tokens. Those tokens gain haste until end of turn.
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ConditionalEffect(
                new ControllerControlsCommander(),
                new CreateTokenEffect(
                        CardType.CREATURE,
                        2,
                        "Goblin",
                        1,
                        1,
                        CardColor.RED,
                        Set.of(),
                        List.of(CardSubtype.GOBLIN),
                        Set.of(),
                        Set.of(),
                        false,
                        false,
                        Map.of(),
                        List.of(),
                        false,
                        false,
                        false,
                        0,
                        Set.of(Keyword.HASTE))));

        // {2}, Sacrifice a Goblin: This creature deals 1 damage to any target.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasSubtypePredicate(CardSubtype.GOBLIN)
                                )),
                                "Sacrifice a Goblin",
                                false
                        ),
                        new DealDamageToAnyTargetEffect(1)),
                "{2}, Sacrifice a Goblin: Siege-Gang Lieutenant deals 1 damage to any target."
        ));
    }
}
