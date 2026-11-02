package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "86")
@CardRegistration(set = "MIC", collectorNumber = "114")
@CardRegistration(set = "DRC", collectorNumber = "91")
public class DreadhordeInvasion extends Card {

    public DreadhordeInvasion() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                new LoseLifeEffect(1), new AmassGoblinsEffect(1, CardSubtype.ZOMBIE)));

        PermanentPredicate qualifyingZombieToken = new PermanentAllOfPredicate(List.of(
                new PermanentIsTokenPredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE),
                new PermanentPowerAtLeastPredicate(6)));
        addTrigger(Triggers.creatureYouControlAttacks(),
                new TriggeringPermanentConditionalEffect(qualifyingZombieToken,
                        new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.TRIGGERING_PERMANENT)));
    }
}
