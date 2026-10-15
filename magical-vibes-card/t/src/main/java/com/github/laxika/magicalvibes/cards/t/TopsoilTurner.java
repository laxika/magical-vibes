package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantActivatedAbilityToMatchingHandCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "YECL", collectorNumber = "16")
public class TopsoilTurner extends Card {

    public TopsoilTurner() {
        ActivatedAbility addTwoGreenMana = new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.GREEN, 2)),
                "{T}: Add {G}{G}."
        );
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PerpetuallyGrantActivatedAbilityToMatchingHandCardsEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.FOREST),
                                new CardSubtypePredicate(CardSubtype.TREEFOLK))),
                        addTwoGreenMana));
    }
}
