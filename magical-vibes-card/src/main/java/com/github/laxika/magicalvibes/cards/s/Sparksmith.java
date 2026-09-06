package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "235")
public class Sparksmith extends Card {

    public Sparksmith() {
        PermanentCount goblinCount = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.GOBLIN), CountScope.ANY_PLAYER);
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new DealDamageToTargetCreatureEffect(goblinCount),
                        new DealDamageToPlayersEffect(goblinCount, DamageRecipient.CONTROLLER)
                ),
                "{T}: This creature deals X damage to target creature and X damage to you, where X is the number of Goblins on the battlefield."
        ));
    }
}
