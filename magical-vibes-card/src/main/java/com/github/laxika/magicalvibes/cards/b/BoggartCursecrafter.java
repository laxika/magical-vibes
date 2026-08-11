package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "ECL", collectorNumber = "206")
public class BoggartCursecrafter extends Card {

    public BoggartCursecrafter() {
        // Whenever another Goblin you control dies, this creature deals 1 damage to each opponent.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.GOBLIN),
                        new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT)));
    }
}
