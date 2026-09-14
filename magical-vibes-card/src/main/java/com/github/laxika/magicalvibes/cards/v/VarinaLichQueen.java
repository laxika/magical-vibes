package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackingCreaturesOfSubtype;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "2X2", collectorNumber = "291")
public class VarinaLichQueen extends Card {

    public VarinaLichQueen() {
        PermanentCount attackingZombies = new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsAttackingPredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE))),
                CountScope.CONTROLLER);

        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new ConditionalEffect(
                new MinimumAttackingCreaturesOfSubtype(1, CardSubtype.ZOMBIE),
                SequenceEffect.of(
                        new DrawCardEffect(attackingZombies),
                        new DiscardEffect(attackingZombies, DiscardRecipient.CONTROLLER),
                        new GainLifeEffect(attackingZombies))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new ExileNCardsFromGraveyardCost(2, null),
                        new CreateTokenEffect(1, "Zombie", 2, 2, CardColor.BLACK,
                                List.of(CardSubtype.ZOMBIE), Set.of(), Set.of(), true)),
                "{2}, Exile two cards from your graveyard: Create a tapped 2/2 black Zombie creature token."));
    }
}
