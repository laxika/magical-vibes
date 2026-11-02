package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "37")
public class Jokulmorder extends Card {

    public Jokulmorder() {
        addStaticEffect(new EntersTappedEffect());
        addStaticEffect(DoesntUntapEffect.self());

        addTrigger(Triggers.enters(ObjectRelation.THIS), new ForcedCostOrElseEffect(
                new SacrificeMultiplePermanentsCost(5, new PermanentIsLandPredicate()),
                List.of(new SacrificeSelfEffect()),
                true));

        addTrigger(Triggers.youPlayLand(), new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.ISLAND),
                new MayEffect(new UntapPermanentsEffect(TapUntapScope.SELF),
                        "Untap Jokulmorder?")));
    }
}
