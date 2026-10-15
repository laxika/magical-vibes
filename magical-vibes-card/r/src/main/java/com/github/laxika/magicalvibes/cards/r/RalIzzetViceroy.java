package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CardsInExile;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetOnControllerSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "195")
public class RalIzzetViceroy extends Card {

    private static final CardAnyOfPredicate INSTANT_OR_SORCERY = new CardAnyOfPredicate(List.of(
            new CardTypePredicate(CardType.INSTANT),
            new CardTypePredicate(CardType.SORCERY)));

    public RalIzzetViceroy() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(LookAtTopCardsEffect.chooseNToHandRestToGraveyard(2, 1)),
                "+1: Look at the top two cards of your library. Put one of them into your hand and the other into your graveyard."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new DealDamageToTargetCreatureEffect(new Sum(
                        new CardsInExile(INSTANT_OR_SORCERY, CountScope.CONTROLLER),
                        new CardsInGraveyard(INSTANT_OR_SORCERY, CountScope.CONTROLLER)))),
                "−3: Ral deals damage to target creature equal to the total number of instant and sorcery cards you own in exile and in your graveyard."
        ));

        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new CreateEmblemEffect(
                        List.of(new DealDamageToAnyTargetOnControllerSpellCastEffect(
                                4, INSTANT_OR_SORCERY, List.of(new DrawCardEffect(2)))),
                        "Whenever you cast an instant or sorcery spell, this emblem deals 4 damage to any target and you draw two cards.")),
                "−8: You get an emblem with \"Whenever you cast an instant or sorcery spell, this emblem deals 4 damage to any target and you draw two cards.\""
        ));
    }
}
