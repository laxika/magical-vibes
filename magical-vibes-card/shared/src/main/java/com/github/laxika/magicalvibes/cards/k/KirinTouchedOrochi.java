package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndCreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardThenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

public class KirinTouchedOrochi extends Card {

    public KirinTouchedOrochi() {
        CreateTokenEffect spiritToken = new CreateTokenEffect(
                "Spirit", 1, 1, null, List.of(CardSubtype.SPIRIT), Set.of(), Set.of());
        var creatureYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate()));
        CardEffect creatureCardMode = new ExileTargetCardFromGraveyardAndCreateTokenEffect(
                new CardTypePredicate(CardType.CREATURE), false, spiritToken, false);
        CardEffect noncreatureCardMode = new ExileTargetCardFromGraveyardThenEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                new QueueReflexiveAbilityEffect(
                        PutCounterOnTargetPermanentEffect.withTargetRestriction(
                                CounterType.PLUS_ONE_PLUS_ONE, 1, creatureYouControl)));

        addEffect(EffectSlot.ON_ATTACK, new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Exile target creature card from a graveyard. When you do, create a 1/1 colorless Spirit creature token.",
                        creatureCardMode),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile target noncreature card from a graveyard. When you do, put a +1/+1 counter on target creature you control.",
                        noncreatureCardMode)
        ))));
    }
}
