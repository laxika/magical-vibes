package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CardsInExile;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Min;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "186")
public class KayaOrzhovUsurper extends Card {

    public KayaOrzhovUsurper() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(ExileGraveyardCardsEffect.targetedFromAnyGraveyardWithEventValue(
                                2, new CardTypePredicate(CardType.CREATURE)),
                        new GainLifeEffect(new Scaled(new Min(new EventValue(), new Fixed(1)), 2))),
                "+1: Exile up to two target cards from a single graveyard. You gain 2 life if at least one creature card was exiled this way."
        ));

        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new ExileTargetPermanentEffect()),
                "−1: Exile target nonland permanent with mana value 1 or less.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                                new PermanentMaxManaValuePredicate(1))),
                        "Target must be a nonland permanent with mana value 1 or less"
                )
        ));

        CardsInExile cardsOwnedInExile = new CardsInExile(null, CountScope.TARGET_PLAYER);
        addActivatedAbility(new ActivatedAbility(
                -5,
                List.of(
                        new DealDamageToPlayersEffect(cardsOwnedInExile, DamageRecipient.TARGET_PLAYER),
                        new GainLifeEffect(cardsOwnedInExile)
                ),
                "−5: Kaya deals damage to target player equal to the number of cards that player owns in exile and you gain that much life.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                )
        ));
    }
}
