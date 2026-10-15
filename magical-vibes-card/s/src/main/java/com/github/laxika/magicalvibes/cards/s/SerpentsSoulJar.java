package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastCardsExiledWithSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "KHC", collectorNumber = "11")
public class SerpentsSoulJar extends Card {

    public SerpentsSoulJar() {
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.ELF),
                        new ExileTriggeringCreatureAndTrackWithSourceEffect()));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PayLifeCost(2),
                        new AllowCastCardsExiledWithSourceUntilEndOfTurnEffect(
                                new CardTypePredicate(CardType.CREATURE), false)),
                "{T}, Pay 2 life: Until end of turn, you may cast a creature spell from among cards "
                        + "exiled with this artifact."));
    }
}
