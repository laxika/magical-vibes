package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMP", collectorNumber = "154")
@CardRegistration(set = "TPR", collectorNumber = "115")
@CardRegistration(set = "VMA", collectorNumber = "139")
public class Sarcomancy extends Card {

    public Sarcomancy() {
        // When this enchantment enters, create a 2/2 black Zombie creature token.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CreateTokenEffect("Zombie", 2, 2, CardColor.BLACK,
                List.of(CardSubtype.ZOMBIE), Set.of(), Set.of()));

        // At the beginning of your upkeep, if there are no Zombies on the battlefield,
        // this enchantment deals 1 damage to you.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new AnyPlayerControlsPermanentCountAtMost(0,
                                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE))),
                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER));
    }
}
