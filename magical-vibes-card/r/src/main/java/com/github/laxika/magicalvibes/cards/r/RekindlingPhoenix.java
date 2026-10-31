package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RIX", collectorNumber = "111")
@CardRegistration(set = "NCC", collectorNumber = "273")
public class RekindlingPhoenix extends Card {

    private static final String NAME = "Rekindling Phoenix";

    public RekindlingPhoenix() {
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                1,
                "Elemental",
                0,
                1,
                CardColor.RED,
                List.of(CardSubtype.ELEMENTAL),
                Set.of(),
                Set.of(),
                // "Sacrifice this creature and return target card..." is one ability: if the token has
                // already left the battlefield, the sacrifice is impossible but the return still happens
                // (CR 609.3), so this is a sequence rather than a sacrifice-contingent "if you do".
                List.of(
                        LegacySlots.triggered(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                        new SacrificeSelfEffect(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardNamedPredicate(NAME))
                                .targetGraveyard(true)
                                .battlefieldEffectGrants(List.of(
                                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)))
                                .battlefieldEffectGrantDuration(EffectDuration.UNTIL_END_OF_TURN)
                                .build()
                )))
        ));
    }
}
