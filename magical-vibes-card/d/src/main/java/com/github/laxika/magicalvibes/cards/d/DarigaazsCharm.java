package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "100")
@CardRegistration(set = "DDE", collectorNumber = "59")
public class DarigaazsCharm extends Card {

    public DarigaazsCharm() {
        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature card from your graveyard to your hand",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardTypePredicate(CardType.CREATURE))
                                .targetGraveyard(true)
                                .build()),
                new ChooseOneEffect.ChooseOneOption(
                        "Darigaaz's Charm deals 3 damage to any target",
                        DealDamageEffect.toAnyTarget(3)),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets +3/+3 until end of turn",
                        BoostEffect.target(3, 3))
        )));
    }
}
