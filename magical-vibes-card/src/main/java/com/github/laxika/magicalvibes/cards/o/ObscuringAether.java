package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.TurnSourceFaceDownEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DTK", collectorNumber = "194")
public class ObscuringAether extends Card {

    public ObscuringAether() {
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardTypePredicate(CardType.CREATURE), new Fixed(1), CostModificationScope.SELF,
                Set.of(), false, true));
        addActivatedAbility(new ActivatedAbility(false, "{1}{G}", List.of(new TurnSourceFaceDownEffect()),
                "{1}{G}: Turn this enchantment face down."));
    }
}
