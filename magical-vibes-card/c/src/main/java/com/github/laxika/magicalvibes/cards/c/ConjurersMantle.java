package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardSharesCreatureTypeWithPermanentsPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "MOC", collectorNumber = "12")
@CardRegistration(set = "MOC", collectorNumber = "99")
public class ConjurersMantle extends Card {

    public ConjurersMantle() {
        addStaticEffect(new StaticBoostEffect(1, 1, Set.of(Keyword.VIGILANCE), Subjects.attachedCreature()));
        addTrigger(Triggers.attacks(),
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(6,
                        // "With that creature": the attacking creature, as it last existed if it has left (the
                        // card's ruling).
                        new CardSharesCreatureTypeWithPermanentsPredicate(Subjects.triggering())));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
