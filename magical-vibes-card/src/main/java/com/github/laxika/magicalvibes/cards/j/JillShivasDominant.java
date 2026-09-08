package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.ShivaWardenOfIce;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "58")
@CardRegistration(set = "FIN", collectorNumber = "378")
@CardRegistration(set = "FIN", collectorNumber = "438")
@CardRegistration(set = "FIN", collectorNumber = "523")
public class JillShivasDominant extends Card {

    public JillShivasDominant() {
        setBackFaceCard(new ShivaWardenOfIce());

        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                )),
                "Target must be another nonland permanent"
        ), 0, 1).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ReturnToHandEffect.target());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{U}{U}",
                List.of(new ExileSelfAndReturnTransformedEffect()),
                "{3}{U}{U}, {T}: Exile Jill, then return it to the battlefield transformed under its owner's control. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    @Override
    public String getBackFaceClassName() {
        return "ShivaWardenOfIce";
    }
}
