package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "185")
public class MorcantsEyes extends Card {

    public MorcantsEyes() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SurveilEffect(1));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{G}{G}",
                List.of(
                        new SacrificeSelfCost(),
                        new CreateTokenEffect(
                                CardType.CREATURE,
                                new CardsInGraveyard(new CardSubtypePredicate(CardSubtype.ELF), CountScope.CONTROLLER),
                                "Elf", 2, 2, CardColor.BLACK, Set.of(CardColor.BLACK, CardColor.GREEN),
                                List.of(CardSubtype.ELF), Set.of(), Set.of(),
                                false, false, Map.of(), List.of(), false, false, false, 0, Set.of()
                        )
                ),
                "{4}{G}{G}, Sacrifice this enchantment: Create X 2/2 black and green Elf creature tokens, where X is the number of Elf cards in your graveyard.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
