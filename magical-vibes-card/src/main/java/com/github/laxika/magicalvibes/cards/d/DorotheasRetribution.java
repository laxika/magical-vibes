package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileInsteadOfGraveyardReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Back face of {@link DorotheaVengefulVictim}. */
public class DorotheasRetribution extends Card {

    public DorotheasRetribution() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_ATTACK,
                        SequenceEffect.of(
                                new CreateTokenEffect(
                                        CardType.CREATURE, 1, "Spirit", 4, 4, CardColor.WHITE, null,
                                        List.of(CardSubtype.SPIRIT), Set.of(Keyword.FLYING), Set.of(),
                                        true, false, Map.of(), List.of(), false, false, false, 0, Set.of()),
                                new SacrificeCreatedPermanentsAtEndOfCombatEffect()),
                        GrantScope.ENCHANTED_CREATURE));
        addEffect(EffectSlot.STATIC, new ExileInsteadOfGraveyardReplacementEffect());
    }
}
