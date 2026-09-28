package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.Overloaded;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToOwnCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "YDFT", collectorNumber = "7")
public class BailOut extends Card {

    public BailOut() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{B}{B}"))));

        SequenceEffect returnAndDamage = SequenceEffect.of(
                new ReturnSourceCardFromGraveyardToBattlefieldEffect(true),
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new GrantEffectToTargetUntilEndOfTurnEffect(EffectSlot.ON_DEATH, returnAndDamage),
                new GrantEffectToOwnCreaturesUntilEndOfTurnEffect(EffectSlot.ON_DEATH, returnAndDamage)));
        target(TargetFilters.creatureYouControl());
    }
}
