package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlledCommanderAsCast;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDiscardHandThenDrawEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToGraveyardCardsEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TDC", collectorNumber = "40")
@CardRegistration(set = "TDC", collectorNumber = "80")
public class WillOfTheJeskai extends Card {

    public WillOfTheJeskai() {
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMoreWhen(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Each player may discard their hand and draw five cards",
                        new EachPlayerMayDiscardHandThenDrawEffect(5)),
                new ChooseOneEffect.ChooseOneOption(
                        "Each instant and sorcery card in your graveyard gains flashback until end of turn",
                        new GrantFlashbackToGraveyardCardsEffect(Set.of(CardType.INSTANT, CardType.SORCERY)))
        ), new ControlledCommanderAsCast()));
    }
}
