package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "81")
@CardRegistration(set = "SLD", collectorNumber = "2469")
@CardRegistration(set = "ARC", collectorNumber = "80")
public class BatwingBrume extends Card {

    public BatwingBrume() {
        // Prevent all combat damage that would be dealt this turn if {W} was spent to cast this spell.
        addSpellEffect(new ConditionalEffect(
                new ColorSpentToCast(ManaColor.WHITE),
                PreventDamageEffect.allCombat()));

        // Each player loses 1 life for each attacking creature they control if {B} was spent to cast this spell.
        addSpellEffect(new ConditionalEffect(
                new ColorSpentToCast(ManaColor.BLACK),
                new LoseLifeEffect(new PermanentCount(new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(), new PermanentIsAttackingPredicate())),
                        CountScope.CONTROLLER), Subjects.eachPlayer())));
    }
}
