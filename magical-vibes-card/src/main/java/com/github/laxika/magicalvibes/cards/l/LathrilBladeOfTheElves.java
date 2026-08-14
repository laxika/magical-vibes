package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "242")
public class LathrilBladeOfTheElves extends Card {

    public LathrilBladeOfTheElves() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new CreateTokenEffect(
                new EventValue(), "Elf Warrior", 1, 1, CardColor.GREEN,
                List.of(CardSubtype.ELF, CardSubtype.WARRIOR), Set.of(), Set.of()));

        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(
                        new TapMultiplePermanentsCost(10, new PermanentHasSubtypePredicate(CardSubtype.ELF), true),
                        new LoseLifeEffect(10, LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(10)),
                "{T}, Tap ten untapped Elves you control: Each opponent loses 10 life and you gain 10 life."));
    }
}
