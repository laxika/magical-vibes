package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.EventStat;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WWK", collectorNumber = "94")
public class TuktukScrapper extends Card {

    public TuktukScrapper() {
        PermanentCount allies = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.ALLY), CountScope.CONTROLLER);

        target(TargetFilters.artifact()).addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.ALLY),
                        new MayEffect(DestroyTargetPermanentThenEffect.ifDiesThisWay(
                                EventStat.NONE,
                                new DealDamageToPlayersEffect(allies, DamageRecipient.TARGET_PLAYER),
                                ThenEffectRecipient.TARGET_CONTROLLER_AS_TARGET,
                                null),
                                "Destroy target artifact?")));
    }
}
