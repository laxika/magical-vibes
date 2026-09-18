package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "79")
public class AbstergoEntertainment extends Card {

    public AbstergoEntertainment() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new AwardAnyColorManaEffect()),
                "{1}, {T}: Add one mana of any color."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new ExileSelfCost(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardIsHistoricPredicate())
                                .targetGraveyard(true)
                                .upTo(true)
                                .build(),
                        new ExileGraveyardCardsEffect(GraveyardExileScope.ALL_PLAYERS)
                ),
                "{3}, {T}, Exile Abstergo Entertainment: Return up to one target historic card "
                        + "from your graveyard to your hand, then exile all graveyards."
        ));
    }
}
