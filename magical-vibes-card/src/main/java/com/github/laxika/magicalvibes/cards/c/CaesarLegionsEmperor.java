package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "PIP", collectorNumber = "1")
@CardRegistration(set = "PIP", collectorNumber = "339")
@CardRegistration(set = "PIP", collectorNumber = "529")
@CardRegistration(set = "PIP", collectorNumber = "867")
@CardRegistration(set = "PIP", collectorNumber = "1064")
public class CaesarLegionsEmperor extends Card {

    public CaesarLegionsEmperor() {
        PermanentAllOfPredicate anotherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));
        PermanentCount creatureTokens = new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsTokenPredicate()
                )),
                CountScope.CONTROLLER);

        ChooseOneEffect chooseTwo = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create two 1/1 red and white Soldier creature tokens with haste that are tapped and attacking",
                        new CreateTokenEffect(
                                CardType.CREATURE, 2, "Soldier", 1, 1,
                                CardColor.RED, Set.of(CardColor.RED, CardColor.WHITE),
                                List.of(CardSubtype.SOLDIER), Set.of(Keyword.HASTE), Set.of(),
                                true, false, Map.of(), List.of(), false, false, false, 0, Set.of()
                        )
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Draw a card and lose 1 life",
                        SequenceEffect.of(new DrawCardEffect(), new LoseLifeEffect(1))
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Caesar deals damage equal to the number of creature tokens you control to target opponent",
                        new DealDamageToPlayersEffect(creatureTokens, DamageRecipient.TARGET_PLAYER),
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                "Target must be an opponent"
                        )
                )
        ), false, 2, 2, false);

        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new MayEffect(
                        new SacrificePermanentThenEffect(anotherCreature, chooseTwo, "another creature"),
                        "Sacrifice another creature?"
                ));
    }
}
