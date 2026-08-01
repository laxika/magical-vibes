package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.Overloaded;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCanBeBlockedOnlyByFilterToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "202")
public class Teleportal extends Card {

    public Teleportal() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}{U}{R}"))));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new BoostTargetCreatureEffect(1, 0),
                new BoostAllOwnCreaturesEffect(1, 0)));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new MakeCreatureUnblockableEffect(),
                new GrantCanBeBlockedOnlyByFilterToOwnCreaturesEffect(
                        null,
                        new PermanentNotPredicate(new PermanentTruePredicate()),
                        "no creatures")));
        target(TargetFilters.creatureYouControl());
    }
}
