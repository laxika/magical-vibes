package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;


@CardRegistration(set = "DRK", collectorNumber = "46")
public class GraveRobbers extends Card {

    public GraveRobbers() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(
                                1, new CardTypePredicate(CardType.ARTIFACT), false),
                        new GainLifeEffect(2)),
                "{B}, {T}: Exile target artifact card from a graveyard. You gain 2 life."));
    }
}
