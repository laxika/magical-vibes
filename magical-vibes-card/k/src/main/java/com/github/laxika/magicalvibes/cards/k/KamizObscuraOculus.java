package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTargetCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerLessThanTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "NCC", collectorNumber = "3")
@CardRegistration(set = "NCC", collectorNumber = "103")
@CardRegistration(set = "NCC", collectorNumber = "188")
public class KamizObscuraOculus extends Card {

    private static final PermanentPredicate ANOTHER_ATTACKER_WITH_LESSER_POWER = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentIsAttackingPredicate(),
            new PermanentNotPredicate(new PermanentIsTargetCardPredicate()),
            new PermanentPowerLessThanTargetPredicate()));

    public KamizObscuraOculus() {
        target(TargetFilters.attackingCreature()).addTrigger(Triggers.youAttack(),
                SequenceEffect.of(
                        new MakeCreatureUnblockableEffect(),
                        new DrawDiscardAndConniveEffect(true),
                        ChoosePermanentsEffect.exactly(1,
                                Subjects.controlledBy(Subjects.you(), ANOTHER_ATTACKER_WITH_LESSER_POWER),
                                "another attacking creature with lesser power"),
                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, Subjects.thisWay(StepKind.CHOOSE))));
    }
}
