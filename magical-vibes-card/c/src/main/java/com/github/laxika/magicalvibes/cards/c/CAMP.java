package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachSourceFortificationToTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetCreatureAndCreateTokenIfSharesManaColorEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "129")
@CardRegistration(set = "PIP", collectorNumber = "657")
public class CAMP extends Card {

    public CAMP() {
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND,
                new PutCounterOnTargetCreatureAndCreateTokenIfSharesManaColorEffect(junkToken()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new AttachSourceFortificationToTargetLandEffect()),
                "Fortify {3}: Attach this Fortification to target land you control. Activate only as a sorcery.",
                TargetFilters.landYouControl(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    private static CreateTokenEffect junkToken() {
        return CreateTokenEffect.ofArtifactToken(
                1,
                "Junk",
                List.of(CardSubtype.JUNK),
                List.of(new ActivatedAbility(
                        true,
                        null,
                        List.of(new SacrificeSelfCost(), new ExileTopCardMayPlayThisTurnEffect(false)),
                        "{T}, Sacrifice this token: Exile the top card of your library. You may play that card this turn. "
                                + "Activate only as a sorcery.",
                        ActivationTimingRestriction.SORCERY_SPEED)));
    }
}
