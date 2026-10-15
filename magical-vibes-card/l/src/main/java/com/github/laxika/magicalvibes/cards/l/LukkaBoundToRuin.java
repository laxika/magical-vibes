package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DivisionMode;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ONE", collectorNumber = "207")
public class LukkaBoundToRuin extends Card {

    public LukkaBoundToRuin() {
        ManaRestriction creatureSpellsOrAbilities = new ManaRestriction.CreatureSpellsOrAbilities();
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(
                        new AwardRestrictedManaEffect(ManaColor.RED, 1, creatureSpellsOrAbilities),
                        new AwardRestrictedManaEffect(ManaColor.GREEN, 1, creatureSpellsOrAbilities)),
                "+1: Add {R}{G}. Spend this mana only to cast creature spells or activate abilities of creatures."
        ));

        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new CreateTokenEffect(
                        CardType.CREATURE,
                        1,
                        "Phyrexian Beast",
                        3,
                        3,
                        CardColor.GREEN,
                        null,
                        List.of(CardSubtype.PHYREXIAN, CardSubtype.BEAST),
                        Set.of(Keyword.TOXIC),
                        Set.of(),
                        false,
                        false,
                        Map.of(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                                new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER)),
                        List.of(),
                        false,
                        false,
                        false,
                        0,
                        Set.of()
                )),
                "-1: Create a 3/3 green Phyrexian Beast creature token with toxic 1."
        ));

        addActivatedAbility(new ActivatedAbility(
                -4,
                List.of(new DealDividedDamageEffect(
                        new GreatestPowerAmongControlled(),
                        null,
                        DivisionMode.CHOSEN,
                        null,
                        0,
                        false,
                        false,
                        false,
                        false,
                        false,
                        true
                )),
                "-4: Lukka deals X damage divided as you choose among any number of target creatures and/or planeswalkers, where X is the greatest power among creatures you control as you activate this ability."
        ));
    }
}
