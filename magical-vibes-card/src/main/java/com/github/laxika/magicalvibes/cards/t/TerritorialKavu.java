package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "216")
public class TerritorialKavu extends Card {

    public TerritorialKavu() {
        BasicLandTypesAmongControlledLands domain = new BasicLandTypesAmongControlledLands();
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(domain, domain));

        addEffect(EffectSlot.ON_ATTACK, new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Discard a card. If you do, draw a card.",
                        new DiscardCardThenEffect(null, new DrawCardEffect(), "a card")),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile up to one target card from a graveyard.",
                        ExileGraveyardCardsEffect.upToOneTargetFromAnyGraveyard())
        ))));
    }
}
