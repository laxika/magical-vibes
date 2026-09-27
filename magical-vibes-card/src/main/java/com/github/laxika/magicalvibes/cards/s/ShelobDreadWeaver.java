package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardExiledWithSourceIntoGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardExiledWithSourceToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "LTC", collectorNumber = "29")
@CardRegistration(set = "LTC", collectorNumber = "112")
public class ShelobDreadWeaver extends Card {

    public ShelobDreadWeaver() {
        addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES,
                new TriggeringCardConditionalEffect(
                        new CardNotPredicate(new CardIsTokenPredicate()),
                        new ExileTriggeringCreatureAndTrackWithSourceEffect()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(
                        new PutCardExiledWithSourceIntoGraveyardCost(
                                new CardTypePredicate(CardType.CREATURE)),
                        SequenceEffect.of(
                                new PutCountersOnSourceEffect(1, 1, 2),
                                new DrawCardEffect(1))),
                "{2}{B}, Put a creature card exiled with Shelob into its owner's graveyard: Put two +1/+1 counters on Shelob. Draw a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}{1}{B}",
                List.of(new ReturnCardExiledWithSourceToBattlefieldEffect(
                        new CardTypePredicate(CardType.CREATURE), true, null,
                        true, false, false, true, 0, false, false)),
                "{X}{1}{B}: Put target creature card with mana value X exiled with Shelob onto the battlefield tapped under your control."
        ));
    }
}
