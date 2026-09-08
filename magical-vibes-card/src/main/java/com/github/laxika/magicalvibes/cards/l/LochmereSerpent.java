package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "195")
public class LochmereSerpent extends Card {

    public LochmereSerpent() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.ISLAND), "Sacrifice an Island"),
                        new MakeCreatureUnblockableEffect(true)
                ),
                "{U}, Sacrifice an Island: This creature can't be blocked this turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.SWAMP), "Sacrifice a Swamp"),
                        new GainLifeEffect(1),
                        new DrawCardEffect(1)
                ),
                "{B}, Sacrifice a Swamp: You gain 1 life and draw a card."
        ));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{U}{B}",
                List.of(
                        new ExileGraveyardCardsEffect(5, GraveyardExileScope.TARGET_CARDS_OPPONENT_GRAVEYARD),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .build()
                ),
                "{U}{B}: Exile five target cards from an opponent's graveyard. Return this card from your graveyard to your hand. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
