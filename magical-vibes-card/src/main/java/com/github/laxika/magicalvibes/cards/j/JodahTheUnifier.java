package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUntilCardWithManaValueLessThanMayCastWithoutPayingEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SpellCastFromHandTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1932")
public class JodahTheUnifier extends Card {

    public JodahTheUnifier() {
        PermanentCount legendaryCreatures = new PermanentCount(
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                legendaryCreatures, legendaryCreatures, GrantScope.ALL_OWN_CREATURES,
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)));

        CardSupertypePredicate legendary = new CardSupertypePredicate(CardSupertype.LEGENDARY);
        CardAllOfPredicate legendaryNonland = new CardAllOfPredicate(List.of(
                legendary,
                new CardNotPredicate(new CardTypePredicate(CardType.LAND))));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastFromHandTriggerEffect(
                legendary,
                List.of(new ExileUntilCardWithManaValueLessThanMayCastWithoutPayingEffect(
                        legendaryNonland, new TargetSpellManaValue()))));
    }
}
