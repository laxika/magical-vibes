package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "CMM", collectorNumber = "733")
@CardRegistration(set = "CMM", collectorNumber = "764")
public class LazotepSliver extends Card {

    public LazotepSliver() {
        // Sliver creatures you control have afflict 2.
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_BECOMES_BLOCKED,
                new LoseLifeEffect(2, LoseLifeRecipient.DEFENDING_PLAYER),
                GrantScope.ALL_OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));

        // Whenever a nontoken Sliver you control dies, amass Slivers 2. The separate death
        // trigger covers Lazotep Sliver itself; ally-death watchers exclude their source.
        CardEffect sliverDeath = new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.SLIVER),
                new AmassGoblinsEffect(2, CardSubtype.SLIVER));
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, sliverDeath);
        addEffect(EffectSlot.ON_DEATH, sliverDeath);
    }
}
