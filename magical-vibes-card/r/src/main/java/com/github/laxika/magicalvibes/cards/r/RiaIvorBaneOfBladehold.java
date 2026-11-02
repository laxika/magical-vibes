package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ToxicEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextCombatDamageByTargetCreatureAndCreateTokensEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ONE", collectorNumber = "214")
public class RiaIvorBaneOfBladehold extends Card {

    public RiaIvorBaneOfBladehold() {
        target(TargetFilters.creature()).addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                new PreventNextCombatDamageByTargetCreatureAndCreateTokensEffect(phyrexianMite()));
    }

    private static CreateTokenEffect phyrexianMite() {
        return new CreateTokenEffect(
                CardType.CREATURE,
                1,
                "Mite",
                1,
                1,
                null,
                null,
                List.of(CardSubtype.PHYREXIAN, CardSubtype.MITE),
                Set.of(Keyword.TOXIC),
                Set.of(CardType.ARTIFACT),
                false,
                false,
                List.of(StaticAbility.of(SequenceEffect.of(new CantBlockEffect(), new ToxicEffect(1)))),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of());
    }
}
