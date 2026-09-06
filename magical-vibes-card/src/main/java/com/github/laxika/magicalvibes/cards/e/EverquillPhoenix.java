package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "IKO", collectorNumber = "114")
public class EverquillPhoenix extends Card {

    public EverquillPhoenix() {
        ActivatedAbility featherAbility = new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificeSelfCost(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardSubtypePredicate(CardSubtype.PHOENIX))
                                .targetGraveyard(true)
                                .enterTapped(true)
                                .build()),
                "{1}, Sacrifice this token: Return target Phoenix card from your graveyard to the battlefield tapped."
        );

        addEffect(EffectSlot.ON_SELF_MUTATES, new CreateTokenEffect(
                CardType.ARTIFACT, 1, "Feather", 0, 0, CardColor.RED, null,
                List.of(), Set.of(), Set.of(), false, false, Map.of(), List.of(featherAbility),
                false, false, false, 0, Set.of()));
    }
}
