package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.DoubleSelfPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.FlipUntilLoseEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetPlayerLibraryForNamedCardToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "380")
public class OkaunEyeOfChaos extends Card {

    private static final String ZNDRSPLT = "Zndrsplt, Eye of Wisdom";

    public OkaunEyeOfChaos() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new SearchTargetPlayerLibraryForNamedCardToHandEffect(ZNDRSPLT),
                "Have target player put Zndrsplt into their hand from their library?",
                null,
                MayChoicePlayer.TARGET_PLAYER));
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                new FlipUntilLoseEffect(null));
        addEffect(EffectSlot.ON_ANY_PLAYER_WINS_COIN_FLIP,
                new DoubleSelfPowerToughnessEffect());
    }
}
