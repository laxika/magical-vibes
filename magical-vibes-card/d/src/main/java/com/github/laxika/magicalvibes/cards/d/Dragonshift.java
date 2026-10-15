package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.Overloaded;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetAllOwnCreaturesBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

/**
 * Until end of turn, target creature you control becomes a blue and red Dragon with base power
 * and toughness 4/4, loses all abilities, and gains flying.
 * <p>
 * Overload {3}{U}{U}{R}{R} (CR 702.96a): paying the overload cost changes "target" to "each", so
 * every creature its controller controls is transformed and, per CR 702.96b, no targets are chosen.
 * <p>
 * The ability loss is applied before the flying grant so the freshly granted flying survives it
 * (CR 613.1f layer 6, applied in timestamp order per CR 613.7).
 */
@CardRegistration(set = "DGM", collectorNumber = "66")
public class Dragonshift extends Card {

    public Dragonshift() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}{U}{U}{R}{R}"))));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new LosesAllAbilitiesEffect(GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN),
                new LosesAllAbilitiesEffect(GrantScope.OWN_CREATURES, EffectDuration.UNTIL_END_OF_TURN)));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new GrantColorUntilEndOfTurnEffect(CardColor.BLUE),
                new GrantColorUntilEndOfTurnEffect(CardColor.BLUE, GrantScope.OWN_CREATURES)));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new GrantColorUntilEndOfTurnEffect(CardColor.RED, true, GrantScope.TARGET, false),
                new GrantColorUntilEndOfTurnEffect(CardColor.RED, true, GrantScope.OWN_CREATURES, false)));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.DRAGON),
                new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.DRAGON, GrantScope.OWN_CREATURES)));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new SetBasePowerToughnessEffect(4, 4),
                new SetAllOwnCreaturesBasePowerToughnessEffect(4, 4)));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.OWN_CREATURES)));
        target(TargetFilters.creatureYouControl());
    }
}
