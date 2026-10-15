package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.e.EsperTerra;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMilledPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "245")
@CardRegistration(set = "FIN", collectorNumber = "323")
@CardRegistration(set = "FIN", collectorNumber = "511")
public class TerraMagicalAdept extends Card {

    public TerraMagicalAdept() {
        setBackFaceCard(new EsperTerra());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MillControllerAndMayReturnMilledPermanentToHandEffect(
                        5, new CardTypePredicate(CardType.ENCHANTMENT)));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}{R}{G}",
                List.of(new ExileSelfAndReturnTransformedEffect()),
                "{4}{R}{G}, {T}: Exile Terra, then return it to the battlefield transformed under its owner's control. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    @Override
    public String getBackFaceClassName() {
        return "EsperTerra";
    }
}
