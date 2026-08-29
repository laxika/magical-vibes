package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMP", collectorNumber = "154")
@CardRegistration(set = "TPR", collectorNumber = "115")
public class Sarcomancy extends Card {

    public Sarcomancy() {
        // When this enchantment enters, create a 2/2 black Zombie creature token.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect("Zombie", 2, 2, CardColor.BLACK,
                List.of(CardSubtype.ZOMBIE), Set.of(), Set.of()));

        // At the beginning of your upkeep, if there are no Zombies on the battlefield,
        // this enchantment deals 1 damage to you.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new AnyPlayerControlsPermanentCountAtMost(0,
                        new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE)),
                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER)));
    }
}
