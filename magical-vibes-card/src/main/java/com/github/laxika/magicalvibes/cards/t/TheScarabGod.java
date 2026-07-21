package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "145")
public class TheScarabGod extends Card {

    public TheScarabGod() {
        PermanentCount zombiesYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE), CountScope.CONTROLLER);

        // At the beginning of your upkeep, each opponent loses X life and you scry X,
        // where X is the number of Zombies you control.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new LoseLifeEffect(zombiesYouControl, LoseLifeRecipient.EACH_OPPONENT),
                new ScryEffect(zombiesYouControl)));

        // {2}{U}{B}: Exile target creature card from a graveyard. Create a token that's a copy of it,
        // except it's a 4/4 black Zombie.
        addActivatedAbility(new ActivatedAbility(
                false, "{2}{U}{B}",
                List.of(new ExileTargetCardFromGraveyardAndCreateTokenCopyEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        false,
                        List.of(CardSubtype.ZOMBIE),
                        false,
                        false,
                        CardColor.BLACK,
                        4,
                        4)),
                "{2}{U}{B}: Exile target creature card from a graveyard. Create a token that's a copy of it, except it's a 4/4 black Zombie."));

        // When The Scarab God dies, return it to its owner's hand at the beginning of the next end step.
        addEffect(EffectSlot.ON_DEATH, new RegisterDelayedReturnCardFromGraveyardToHandEffect(null));
    }
}
