package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentCreaturesInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardExiledWithSourceToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "96")
public class TheDarknessCrystal extends Card {

    public TheDarknessCrystal() {
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardColorPredicate(CardColor.BLACK), 1, CostModificationScope.SELF));
        addEffect(EffectSlot.STATIC,
                ExileOpponentCreaturesInsteadOfDyingEffect.withLifeGain(2));
        addActivatedAbility(new ActivatedAbility(
                true, "{4}{B}{B}",
                List.of(ReturnCardExiledWithSourceToBattlefieldEffect.targetedCreature(true, 2)),
                "Put target creature card exiled with The Darkness Crystal onto the battlefield tapped under your control with two additional +1/+1 counters on it."));
    }
}
