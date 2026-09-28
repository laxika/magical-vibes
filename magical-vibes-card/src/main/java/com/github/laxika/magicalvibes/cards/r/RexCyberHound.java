package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardFromGraveyardWithBrainCounterEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfCardsExiledWithBrainCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "117")
@CardRegistration(set = "PIP", collectorNumber = "427")
@CardRegistration(set = "PIP", collectorNumber = "645")
@CardRegistration(set = "PIP", collectorNumber = "955")
public class RexCyberHound extends Card {

    public RexCyberHound() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, SequenceEffect.of(
                new MillEffect(2, MillRecipient.TARGET_PLAYER),
                new EnergyCountersEffect(2)));
        addEffect(EffectSlot.STATIC, new GainActivatedAbilitiesOfCardsExiledWithBrainCountersEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new PayEnergyCost(2), new ExileTargetCreatureCardFromGraveyardWithBrainCounterEffect()),
                "Pay {E}{E}: Choose target creature card in a graveyard. Exile it with a brain counter on it. Activate only as a sorcery.",
                new GraveyardCardPredicateTargetFilter(
                        new CardTypePredicate(CardType.CREATURE), GraveyardSearchScope.ALL_GRAVEYARDS),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
