package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.ExileEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.TriggeringCardMatches;

@CardRegistration(set = "CHK", collectorNumber = "265")
public class OathkeeperTakenosDaisho extends Card {

    public OathkeeperTakenosDaisho() {
        // Equipped creature gets +3/+1.
        addStaticEffect(new StaticBoostEffect(3, 1, GrantScope.EQUIPPED_CREATURE));

        // Whenever equipped creature dies, return that card to the battlefield under your control
        // if it's a Samurai card.
        addTrigger(Triggers.creatureDies(ObjectRelation.EQUIPPED), new ConditionalEffect(
                new TriggeringCardMatches(new CardSubtypePredicate(CardSubtype.SAMURAI)),
                new ReturnDyingCreatureToBattlefieldEffect(false)));

        // When Oathkeeper is put into a graveyard from the battlefield, exile equipped creature.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ExileEquippedCreatureEffect());

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
