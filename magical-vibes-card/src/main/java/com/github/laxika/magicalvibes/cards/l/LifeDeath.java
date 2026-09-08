package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "APC", collectorNumber = "130")
public class LifeDeath extends Card {

    public LifeDeath() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Life — All lands you control become 1/1 creatures until end of turn. They're still lands",
                        new AnimatePermanentsEffect(1, 1, List.of(), Set.of(), null,
                                Set.of(CardType.CREATURE), GrantScope.OWN_LANDS, EffectDuration.UNTIL_END_OF_TURN)
                ).withManaCost("{G}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Death — Return target creature card from your graveyard to the battlefield. You lose life equal to its mana value",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardTypePredicate(CardType.CREATURE))
                                .targetGraveyard(true)
                                .loseLifeEqualToManaValue(true)
                                .build()
                ).withManaCost("{1}{B}")
        )));
    }
}
