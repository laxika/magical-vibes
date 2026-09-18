package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.Overloaded;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetAllOwnCreaturesBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

/**
 * Until end of turn, target creature you control becomes a green Wurm with base power and
 * toughness 6/4.
 * <p>
 * Overload {4}{G}{G} (CR 702.96a): paying the overload cost instead of {G} changes "target" to
 * "each", so the same changes apply to every creature its controller controls and, per CR 702.96b,
 * no targets are chosen.
 */
@CardRegistration(set = "MH1", collectorNumber = "179")
public class ScaleUp extends Card {

    public ScaleUp() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{4}{G}{G}"))));

        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new GrantColorUntilEndOfTurnEffect(CardColor.GREEN),
                new GrantColorUntilEndOfTurnEffect(CardColor.GREEN, GrantScope.OWN_CREATURES)));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.WURM),
                new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.WURM, GrantScope.OWN_CREATURES)));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new SetBasePowerToughnessEffect(6, 4),
                new SetAllOwnCreaturesBasePowerToughnessEffect(6, 4)));

        target(TargetFilters.creatureYouControl());
    }
}
