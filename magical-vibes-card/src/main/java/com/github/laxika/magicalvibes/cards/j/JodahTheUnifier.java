package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.BoostLegendaryCreaturesByOtherLegendaryCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUntilCardWithManaValueLessThanMayCastWithoutPayingEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SpellCastFromHandTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1932")
@CardRegistration(set = "DMU", collectorNumber = "203")
@CardRegistration(set = "FCA", collectorNumber = "17")
public class JodahTheUnifier extends Card {

    public JodahTheUnifier() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.ALL_OWN_CREATURES,
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)));
        addEffect(EffectSlot.STATIC, new BoostLegendaryCreaturesByOtherLegendaryCreaturesEffect(1, 1));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardSupertypePredicate(CardSupertype.LEGENDARY),
                List.of(new CascadeEffect(new CardSupertypePredicate(CardSupertype.LEGENDARY))),
                new StackEntryCastFromZonePredicate(Zone.HAND)));
    }
}
