package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ActivateCreatureAbilitiesAsThoughHasteEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "218")
public class TyvarJubilantBrawler extends Card {

    public TyvarJubilantBrawler() {
        addEffect(EffectSlot.STATIC, new ActivateCreatureAbilitiesAsThoughHasteEffect());

        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET, new PermanentIsCreaturePredicate())),
                "+1: Untap up to one target creature.",
                TargetFilters.creature(), +1, null, null,
                List.of(), 0, 1));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(SequenceEffect.of(
                        new MillEffect(3, MillRecipient.CONTROLLER),
                        new MayEffect(
                                ReturnCardFromGraveyardEffect.builder()
                                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                        .filter(new CardAllOfPredicate(List.of(
                                                new CardTypePredicate(CardType.CREATURE),
                                                new CardMaxManaValuePredicate(2))))
                                        .build(),
                                "Return a creature card with mana value 2 or less from your graveyard to the battlefield?"))),
                "−2: Mill three cards, then you may return a creature card with mana value 2 or less "
                        + "from your graveyard to the battlefield."));
    }
}
