package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentAsEntersOrGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "137")
public class BalduvianTradingPost extends Card {

    public BalduvianTradingPost() {
        // If Balduvian Trading Post would enter, sacrifice an untapped Mountain instead. If you do,
        // put this land onto the battlefield. If you don't, put it into its owner's graveyard.
        addEffect(EffectSlot.STATIC, new SacrificePermanentAsEntersOrGraveyardEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN),
                        new PermanentNotPredicate(new PermanentIsTappedPredicate())
                )),
                "an untapped Mountain"));

        // {T}: Add {C}{R}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS), new AwardManaEffect(ManaColor.RED)),
                "{T}: Add {C}{R}."
        ));

        // {1}, {T}: Balduvian Trading Post deals 1 damage to target attacking creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new DealDamageToTargetCreatureEffect(1)),
                "{1}, {T}: Balduvian Trading Post deals 1 damage to target attacking creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsAttackingPredicate(),
                        "Target must be an attacking creature"
                )
        ));
    }
}
