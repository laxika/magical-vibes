package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsOneThenDrawsForEachCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "231")
public class KefkaCourtMage extends Card {

    public KefkaCourtMage() {
        setBackFaceCard(new KefkaRulerOfRuin());

        EachPlayerDiscardsOneThenDrawsForEachCardTypeEffect discardTrigger =
                new EachPlayerDiscardsOneThenDrawsForEachCardTypeEffect();
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, discardTrigger);
        addEffect(EffectSlot.ON_ATTACK, discardTrigger);

        addActivatedAbility(new ActivatedAbility(
                false,
                "{8}",
                List.of(
                        new SacrificePermanentsEffect(1, new PermanentTruePredicate(),
                                SacrificeRecipient.EACH_OPPONENT),
                        new TransformSelfEffect()),
                "{8}: Each opponent sacrifices a permanent of their choice. Transform Kefka. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    @Override
    public String getBackFaceClassName() {
        return "KefkaRulerOfRuin";
    }
}
