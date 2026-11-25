package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "135")
public class RedcapMelee extends Card {

    public RedcapMelee() {
        addSpellEffect(DealDamageEffect.toTargetCreatureOrPlaneswalker(4));
        addSpellEffect(new ConditionalEffect(
                HappenedThisWay.toA(StepKind.DEAL_DAMAGE,
                        new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.RED)))),
                new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(), Subjects.you())));
    }
}
