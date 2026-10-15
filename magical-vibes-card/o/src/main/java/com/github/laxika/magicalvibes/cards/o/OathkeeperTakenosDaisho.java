package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.ExileEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "CHK", collectorNumber = "265")
public class OathkeeperTakenosDaisho extends Card {

    public OathkeeperTakenosDaisho() {
        // Equipped creature gets +3/+1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, 1, GrantScope.EQUIPPED_CREATURE));

        // Whenever equipped creature dies, return that card to the battlefield under your control
        // if it's a Samurai card.
        addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.SAMURAI),
                new ReturnDyingCreatureToBattlefieldEffect(false)
        ));

        // When Oathkeeper is put into a graveyard from the battlefield, exile equipped creature.
        addEffect(EffectSlot.ON_DEATH, new ExileEquippedCreatureEffect());

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
