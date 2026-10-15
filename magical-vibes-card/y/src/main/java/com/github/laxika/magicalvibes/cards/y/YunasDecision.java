package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIC", collectorNumber = "74")
@CardRegistration(set = "FIC", collectorNumber = "125")
public class YunasDecision extends Card {

    public YunasDecision() {
        CardTypePredicate creatureCard = new CardTypePredicate(CardType.CREATURE);
        CardTypePredicate landCard = new CardTypePredicate(CardType.LAND);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Continue the Pilgrimage — Sacrifice a creature. If you do, draw a card, then you may put a creature card and/or a land card from your hand onto the battlefield",
                        new SacrificePermanentThenEffect(
                                new PermanentIsCreaturePredicate(),
                                SequenceEffect.of(
                                        new DrawCardEffect(),
                                        new MayEffect(
                                                new PutCardToBattlefieldEffect(creatureCard, "creature"),
                                                "Put a creature card from your hand onto the battlefield?"),
                                        new MayEffect(
                                                new PutCardToBattlefieldEffect(landCard, "land"),
                                                "Put a land card from your hand onto the battlefield?")),
                                "a creature")),
                new ChooseOneEffect.ChooseOneOption(
                        "Find Another Way — Return one or two target permanent cards from your graveyard to your hand",
                        new ReturnTargetCardsFromGraveyardToHandEffect(
                                new CardIsPermanentPredicate(),
                                2,
                                null,
                                false,
                                false,
                                1,
                                false,
                                Set.of(),
                                false,
                                false,
                                List.of(),
                                false,
                                false,
                                null,
                                false))
        )));
    }
}
