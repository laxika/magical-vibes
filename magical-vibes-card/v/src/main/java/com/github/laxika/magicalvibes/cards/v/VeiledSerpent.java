package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.SourceIsEnchantment;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "110")
@CardRegistration(set = "DMR", collectorNumber = "72")
public class VeiledSerpent extends Card {

    public VeiledSerpent() {
        addCycling("{2}");
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, SpellCastTriggerEffect.withIntervening(
                null,
                // The intervening "if" is rechecked before each effect, so the grant comes first: once the
                // Serpent becomes a creature it is no longer an enchantment.
                List.of(new GrantStaticEffectToSourceEffect(new GrantEffectEffect(
                                new CantAttackUnlessEffect(
                                        new DefendingPlayerControlsPermanent(
                                                new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                                        "an Island"),
                                GrantScope.SELF)),
                        new BecomeCreatureEffect(4, 4, CardSubtype.SERPENT)),
                new SourceIsEnchantment()));
    }
}
