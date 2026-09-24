package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.MinimumMatchingAttackers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "861")
@CardRegistration(set = "SLD", collectorNumber = "1355")
public class VarinaLichQueen extends Card {

    public VarinaLichQueen() {
        SequenceEffect attackEffect = SequenceEffect.of(
                new DrawCardEffect(new EventValue()),
                new DiscardEffect(new EventValue(), DiscardRecipient.CONTROLLER),
                new GainLifeEffect(new EventValue()));
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new ConditionalEffect(
                new MinimumMatchingAttackers(1, new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE)),
                attackEffect));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new ExileNCardsFromGraveyardCost(2, null),
                        new CreateTokenEffect(
                                1, "Zombie", 2, 2, CardColor.BLACK,
                                List.of(CardSubtype.ZOMBIE), Set.of(), Set.of(), true)
                ),
                "{2}, Exile two cards from your graveyard: Create a tapped 2/2 black Zombie creature token."
        ));
    }
}
