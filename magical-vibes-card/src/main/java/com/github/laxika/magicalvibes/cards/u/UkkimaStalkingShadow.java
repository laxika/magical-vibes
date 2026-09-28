package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetPlayerLibraryForNamedCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "C20", collectorNumber = "17")
public class UkkimaStalkingShadow extends Card {

    private static final String PARTNER_NAME = "Cazur, Ruthless Stalker";

    public UkkimaStalkingShadow() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchTargetPlayerLibraryForNamedCardToHandEffect(PARTNER_NAME),
                "Have target player put Cazur, Ruthless Stalker into their hand from their library?",
                null,
                MayChoicePlayer.TARGET_PLAYER));

        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, SequenceEffect.of(
                new DealDamageToPlayersEffect(new SourcePower(), DamageRecipient.TARGET_PLAYER),
                new GainLifeEffect(new SourcePower())));
    }
}
