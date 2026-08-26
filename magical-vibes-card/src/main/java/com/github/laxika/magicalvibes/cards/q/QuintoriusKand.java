package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscoverEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "238")
public class QuintoriusKand extends Card {

    public QuintoriusKand() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(
                        new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT),
                        new GainLifeEffect(2)
                ),
                new StackEntryCastFromZonePredicate(Zone.EXILE)));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new CreateTokenEffect("Spirit", 3, 2, CardColor.RED,
                        Set.of(CardColor.RED, CardColor.WHITE), List.of(CardSubtype.SPIRIT))),
                "+1: Create a 3/2 red and white Spirit creature token."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new DiscoverEffect(4)),
                "-3: Discover 4."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        ExileGraveyardCardsEffect.targetedFromControllerGraveyardMayPlayThisTurn(),
                        new AwardManaEffect(ManaColor.RED, new EventValue())
                ),
                "-6: Exile any number of target cards from your graveyard. Add {R} for each card exiled this way. You may play those cards this turn.",
                null,
                -6,
                null,
                null,
                List.of(new GraveyardCardPredicateTargetFilter(
                        null, GraveyardSearchScope.CONTROLLERS_GRAVEYARD)),
                0,
                Integer.MAX_VALUE
        ));
    }
}
