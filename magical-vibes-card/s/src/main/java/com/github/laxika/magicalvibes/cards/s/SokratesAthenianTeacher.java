package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PreventCombatDamageToPlayerAndDrawHalfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "67")
@CardRegistration(set = "ACR", collectorNumber = "121")
public class SokratesAthenianTeacher extends Card {

    public SokratesAthenianTeacher() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceUntapped(),
                new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF)));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GrantStaticEffectToTargetUntilEndOfTurnEffect(
                        new PreventCombatDamageToPlayerAndDrawHalfEffect())),
                "Sokratic Dialogue — {T}: Until end of turn, target creature gains \"If this creature would deal combat damage to a player, prevent that damage. This creature's controller and that player each draw half that many cards, rounded down.\"",
                TargetFilters.creature()));
    }
}
