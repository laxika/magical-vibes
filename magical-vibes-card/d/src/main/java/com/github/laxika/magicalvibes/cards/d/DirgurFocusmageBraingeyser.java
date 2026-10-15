package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.Braingeyser;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;

import java.util.List;

@CardRegistration(set = "SOC", collectorNumber = "18")
@CardRegistration(set = "SOC", collectorNumber = "68")
public class DirgurFocusmageBraingeyser extends Card {

    private static final CardAnyOfPredicate INSTANT_OR_SORCERY = new CardAnyOfPredicate(
            List.of(new CardTypePredicate(CardType.INSTANT), new CardTypePredicate(CardType.SORCERY))
    );

    private static final CardAllOfPredicate HIGH_VALUE_INSTANT_OR_SORCERY = new CardAllOfPredicate(
            List.of(INSTANT_OR_SORCERY, new CardMinManaValuePredicate(5, true))
    );

    public DirgurFocusmageBraingeyser() {
        setBackFaceCard(new Braingeyser());

        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                INSTANT_OR_SORCERY, 1, CostModificationScope.SELF));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                HIGH_VALUE_INSTANT_OR_SORCERY,
                List.of(new BecomePreparedEffect()),
                new StackEntryCastFromZonePredicate(Zone.HAND)));
    }

    @Override
    public String getBackFaceClassName() {
        return "Braingeyser";
    }
}
