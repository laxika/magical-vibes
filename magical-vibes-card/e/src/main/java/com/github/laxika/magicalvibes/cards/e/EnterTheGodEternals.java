package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WAR", collectorNumber = "196")
@CardRegistration(set = "SCD", collectorNumber = "227")
public class EnterTheGodEternals extends Card {

    public EnterTheGodEternals() {
        target(TargetFilters.creature()).addSpellEffect(DealDamageEffect.toTargetCreature(4));
        addSpellEffect(new GainLifeEffect(new AmountThisWay(StepKind.DEAL_DAMAGE), Subjects.you()));
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player"))
                .addSpellEffect(new MillEffect(4, Subjects.targetPlayer()));

        addSpellEffect(new AmassGoblinsEffect(4, CardSubtype.ZOMBIE));
    }
}
