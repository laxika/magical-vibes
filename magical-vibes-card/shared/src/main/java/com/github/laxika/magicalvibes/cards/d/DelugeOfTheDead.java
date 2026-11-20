package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.CardType;


/**
 * Back face of Invasion of Innistrad.
 */
public class DelugeOfTheDead extends Card {

    public DelugeOfTheDead() {
        // When this enchantment enters, create two 2/2 black Zombie creature tokens.
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.blackZombie(2));

        // {2}{B}: Exile target card from a graveyard. If it was a creature card, create a 2/2
        // black Zombie creature token.
        addActivatedAbility(new ActivatedAbility(false, "{2}{B}",
                List.of(ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(1, null, false),
                        new ConditionalEffect(
                                HappenedThisWay.toACard(StepKind.EXILE, new CardTypePredicate(CardType.CREATURE)),
                                CreateTokenEffect.blackZombie(1))),
                "Exile target card from a graveyard. If it was a creature card, create a 2/2 black Zombie creature token."));
    }
}
