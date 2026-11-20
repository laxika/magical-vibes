package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;


public class KirinTouchedOrochi extends Card {

    public KirinTouchedOrochi() {
        CreateTokenEffect spiritToken = new CreateTokenEffect(
                "Spirit", 1, 1, null, List.of(CardSubtype.SPIRIT), Set.of(), Set.of());
        var creatureYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate()));
        CardEffect creatureCardMode = SequenceEffect.of(
                ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(
                        1, new CardTypePredicate(CardType.CREATURE), false),
                new ConditionalEffect(HappenedThisWay.exiled(), new QueueReflexiveAbilityEffect(spiritToken)));
        CardEffect noncreatureCardMode = SequenceEffect.of(
                ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(
                        1, new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)), false),
                new ConditionalEffect(HappenedThisWay.exiled(), new QueueReflexiveAbilityEffect(
                        PutCounterOnTargetPermanentEffect.withTargetRestriction(
                                CounterType.PLUS_ONE_PLUS_ONE, 1, creatureYouControl))));

        addTrigger(Triggers.attacks(), new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Exile target creature card from a graveyard. When you do, create a 1/1 colorless Spirit creature token.",
                        creatureCardMode),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile target noncreature card from a graveyard. When you do, put a +1/+1 counter on target creature you control.",
                        noncreatureCardMode)
        ))));
    }
}
