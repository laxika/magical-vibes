package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventionScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "9")
public class BattlefieldMedic extends Card {

    public BattlefieldMedic() {
        PermanentCount clericsOnBattlefield = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.CLERIC), CountScope.ANY_PLAYER);
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PreventDamageEffect(
                        PreventionScope.NEXT_TO_TARGET_CREATURE,
                        clericsOnBattlefield,
                        false,
                        null,
                        null,
                        null)),
                "{T}: Prevent the next X damage that would be dealt to target creature this turn, where X is the number of Clerics on the battlefield.",
                TargetFilters.creature()
        ));
    }
}
