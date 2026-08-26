package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotYetChosenThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "137")
@CardRegistration(set = "LCI", collectorNumber = "294")
public class BreechesEagerPillager extends Card {

    public BreechesEagerPillager() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.PIRATE),
                        new ChooseModeNotYetChosenThisTurnEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption(
                                        "Create a Treasure token", CreateTokenEffect.ofTreasureToken(1)),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Target creature can't block this turn",
                                        new CantBlockThisTurnEffect(TapUntapScope.TARGET),
                                        TargetFilters.creature()),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Exile the top card of your library. You may play it this turn.",
                                        new ExileTopCardMayPlayThisTurnEffect(false))))));
    }
}
