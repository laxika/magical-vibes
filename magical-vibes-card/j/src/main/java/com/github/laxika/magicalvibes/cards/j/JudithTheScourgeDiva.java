package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RNA", collectorNumber = "185")
@CardRegistration(set = "2X2", collectorNumber = "234")
@CardRegistration(set = "MUL", collectorNumber = "45")
@CardRegistration(set = "MUL", collectorNumber = "110")
@CardRegistration(set = "MUL", collectorNumber = "175")
@CardRegistration(set = "CMM", collectorNumber = "341")
public class JudithTheScourgeDiva extends Card {

    private static final DealDamageToAnyTargetEffect DEATH_DAMAGE = new DealDamageToAnyTargetEffect(1);
    private static final CardEffect NON_TOKEN_DEATH_DAMAGE =
            DEATH_DAMAGE;

    public JudithTheScourgeDiva() {
        addStaticEffect(new StaticBoostEffect(1, 0, GrantScope.OWN_CREATURES));
        addTrigger(Triggers.creatureDies(
                        new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                        ObjectRelation.YOU_CONTROL),
                NON_TOKEN_DEATH_DAMAGE);
    }
}
