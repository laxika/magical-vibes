package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIC", collectorNumber = "52")
@CardRegistration(set = "FIC", collectorNumber = "119")
public class Transpose extends Card {

    public Transpose() {
        addSpellEffect(new DrawCardEffect(1));
        addSpellEffect(new DiscardEffect(1, DiscardRecipient.CONTROLLER));
        addSpellEffect(new LoseLifeEffect(1));
        addSpellEffect(new ConditionalEffect(new CastFromZone(Zone.HAND), new CreateTokenEffect(
                1, "Wizard", 0, 1, CardColor.BLACK, List.of(CardSubtype.WIZARD), Set.of(), Set.of(), List.of(
                        new TriggeredAbility(Triggers.youCastSpell()
                                        .withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT))))));
    }
}
