package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TotalManaValueOfCardsOwnedInExile;
import com.github.laxika.magicalvibes.model.condition.CardPutIntoExileThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromLibraryInsteadOfLifePaymentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfTargetPlayerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "78")
public class AshiokWickedManipulator extends Card {

    public AshiokWickedManipulator() {
        addEffect(EffectSlot.STATIC, new ExileCardsFromLibraryInsteadOfLifePaymentEffect());

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(LookAtTopCardsEffect.chooseOneToHandRestToExile(new Fixed(2))),
                "+1: Look at the top two cards of your library. Exile one of them and put the other into your hand."
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CreateTokenEffect(
                        CardType.CREATURE,
                        2,
                        "Nightmare",
                        1,
                        1,
                        CardColor.BLACK,
                        Set.of(),
                        List.of(CardSubtype.NIGHTMARE),
                        Set.of(),
                        Set.of(),
                        false,
                        false,
                        Map.of(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                                new ConditionalEffect(
                                        new CardPutIntoExileThisTurn(),
                                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE))),
                        List.of(),
                        false,
                        false,
                        false,
                        0,
                        Set.of()
                )),
                "\u22122: Create two 1/1 black Nightmare creature tokens with \"At the beginning of combat on your turn, if a card was put into exile this turn, put a +1/+1 counter on this token.\""
        ));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new ExileTopCardsOfTargetPlayerLibraryEffect(
                        new TotalManaValueOfCardsOwnedInExile())),
                "\u22127: Target player exiles the top X cards of their library, where X is the total mana value of cards you own in exile.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Must target a player"
                )
        ));
    }
}
