package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "149")
@CardRegistration(set = "FIN", collectorNumber = "464")
public class QueenBrahne extends Card {

    public QueenBrahne() {
        // Prowess is applied by the engine from the Scryfall keyword, so no explicit boost trigger is added here.
        addTrigger(Triggers.attacks(), new CreateTokenEffect(
                CardType.CREATURE, 1, "Wizard", 0, 1, CardColor.BLACK, null,
                List.of(CardSubtype.WIZARD), Set.of(), Set.of(), false, false,
                List.of(
                        new TriggeredAbility(Triggers.youCastSpell()
                                        .withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT))),
                List.of(), false, false, false, 0, Set.of()));
    }
}
