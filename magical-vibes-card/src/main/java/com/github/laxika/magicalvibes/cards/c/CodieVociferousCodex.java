package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.CantCastSpellTypeEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "253")
public class CodieVociferousCodex extends Card {

    public CodieVociferousCodex() {
        addEffect(EffectSlot.STATIC, new CantCastSpellTypeEffect(Set.of(
                CardType.ARTIFACT,
                CardType.BATTLE,
                CardType.CREATURE,
                CardType.ENCHANTMENT,
                CardType.PLANESWALKER)));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(
                        new AwardManaEffect(ManaColor.WHITE),
                        new AwardManaEffect(ManaColor.BLUE),
                        new AwardManaEffect(ManaColor.BLACK),
                        new AwardManaEffect(ManaColor.RED),
                        new AwardManaEffect(ManaColor.GREEN),
                        new RegisterDelayedControllerSpellCastTriggerEffect(
                                null,
                                List.of(new CascadeEffect(true)),
                                true,
                                false)),
                "{4}, {T}: Add {W}{U}{B}{R}{G}. When you next cast a spell this turn, exile cards from the top of your library until you exile an instant or sorcery card with lesser mana value. Until end of turn, you may cast that card without paying its mana cost. Put each other card exiled this way on the bottom of your library in a random order."));
    }
}
