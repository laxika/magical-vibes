package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentCostForTargetingControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "M21", collectorNumber = "60")
public class PursuedWhale extends Card {

    public PursuedWhale() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EachOpponentCreatesTokenEffect(new CreateTokenEffect(
                CardType.CREATURE, 1, "Pirate", 1, 1, CardColor.RED, null,
                List.of(CardSubtype.PIRATE), Set.of(), Set.of(), false, false,
                Map.of(EffectSlot.STATIC, SequenceEffect.of(
                        new CantBlockEffect(),
                        new MustAttackEffect(GrantScope.ALL_OWN_CREATURES))),
                List.of(), false, false, false, 0, Set.of())));
        addEffect(EffectSlot.STATIC, new IncreaseOpponentCostForTargetingControlledPermanentEffect(
                new PermanentIsSourcePermanentPredicate(), 3, false));
    }
}
