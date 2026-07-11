package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "142")
public class DivinersWand extends Card {

    public DivinersWand() {
        // Equipped creature has "Whenever you draw a card, this creature gets +1/+1 and gains
        // flying until end of turn". Modeled as a draw trigger on the Equipment that boosts the
        // attached creature.
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS,
                new BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect(1, 1, Keyword.FLYING));

        // Equipped creature has "{4}: Draw a card."
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        false,
                        "{4}",
                        List.of(new DrawCardEffect(1)),
                        "{4}: Draw a card."
                ),
                GrantScope.EQUIPPED_CREATURE
        ));

        // Whenever a Wizard creature enters, you may attach this Equipment to it.
        addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.WIZARD),
                        new AttachSourceEquipmentToEnteringCreatureEffect()));

        // Equip {3}
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
