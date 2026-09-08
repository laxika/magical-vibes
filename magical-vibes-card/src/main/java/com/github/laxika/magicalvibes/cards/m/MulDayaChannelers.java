package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TopCardOfLibraryType;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PlayWithTopCardRevealedEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "198")
public class MulDayaChannelers extends Card {

    public MulDayaChannelers() {
        // Play with the top card of your library revealed.
        addEffect(EffectSlot.STATIC, new PlayWithTopCardRevealedEffect());
        // As long as the top card of your library is a creature card, this creature gets +3/+3.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new TopCardOfLibraryType(CardType.CREATURE),
                new StaticBoostEffect(3, 3, GrantScope.SELF)));
        // As long as the top card of your library is a land card, this creature has
        // "{T}: Add two mana of any one color."
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new TopCardOfLibraryType(CardType.LAND),
                new GrantActivatedAbilityEffect(
                        new ActivatedAbility(true, null, List.of(new AwardAnyColorManaEffect(2)),
                                "{T}: Add two mana of any one color."),
                        GrantScope.SELF)));
    }
}
