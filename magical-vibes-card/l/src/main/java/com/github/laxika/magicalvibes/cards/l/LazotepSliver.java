package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMM", collectorNumber = "733")
@CardRegistration(set = "CMM", collectorNumber = "764")
public class LazotepSliver extends Card {

    public LazotepSliver() {
        // Sliver creatures you control have afflict 2.
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.becomesBlocked(),
                new LoseLifeEffect(2, LoseLifeRecipient.DEFENDING_PLAYER)),
                GrantScope.ALL_OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));

        // Whenever a nontoken Sliver you control dies, amass Slivers 2. Lazotep Sliver is one of them.
        CardEffect sliverDeath = new AmassGoblinsEffect(2, CardSubtype.SLIVER);
        addTrigger(Triggers.nontokenCreatureDies(
                        new PermanentHasSubtypePredicate(CardSubtype.SLIVER),
                        ObjectRelation.YOU_CONTROL),
                sliverDeath);
    }
}
