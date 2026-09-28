package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "DMC", collectorNumber = "4")
@CardRegistration(set = "DMC", collectorNumber = "79")
public class ShanidSleepersScourge extends Card {

    public ShanidSleepersScourge() {
        PermanentPredicate otherLegendaryCreature = new PermanentAllOfPredicate(List.of(
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.MENACE, GrantScope.ALL_OWN_CREATURES, otherLegendaryCreature));

        CardSupertypePredicate legendaryCard = new CardSupertypePredicate(CardSupertype.LEGENDARY);
        SequenceEffect drawAndLoseLife = SequenceEffect.of(new DrawCardEffect(), new LoseLifeEffect(1));
        addEffect(EffectSlot.ON_CONTROLLER_PLAYS_LAND,
                new TriggeringCardConditionalEffect(legendaryCard, drawAndLoseLife));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(legendaryCard, List.of(drawAndLoseLife)));
    }
}
