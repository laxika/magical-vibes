package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "168")
public class WallOfForgottenPharaohs extends Card {

    public WallOfForgottenPharaohs() {
        // {T}: This creature deals 1 damage to target player or planeswalker.
        // Activate only if you control a Desert or there is a Desert card in your graveyard.
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new DealDamageToTargetPlayerOrPlaneswalkerEffect(1)),
                "{T}: Wall of Forgotten Pharaohs deals 1 damage to target player or planeswalker. "
                        + "Activate only if you control a Desert or there is a Desert card in your graveyard.")
                .withActivationCondition(
                        new AnyOf(List.of(
                                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.DESERT)),
                                new GraveyardCardThreshold(1, new CardSubtypePredicate(CardSubtype.DESERT)))),
                        "Activate only if you control a Desert or there is a Desert card in your graveyard"));
    }
}
