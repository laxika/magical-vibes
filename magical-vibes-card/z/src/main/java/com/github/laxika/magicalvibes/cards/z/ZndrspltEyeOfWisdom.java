package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipUntilLoseEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetPlayerLibraryForNamedCardToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "379")
public class ZndrspltEyeOfWisdom extends Card {

    private static final String OKAUN = "Okaun, Eye of Chaos";

    public ZndrspltEyeOfWisdom() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new SearchTargetPlayerLibraryForNamedCardToHandEffect(OKAUN),
                "Have target player put Okaun into their hand from their library?",
                null,
                MayChoicePlayer.TARGET_PLAYER));
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                new FlipUntilLoseEffect(null));
        addEffect(EffectSlot.ON_ANY_PLAYER_WINS_COIN_FLIP, new DrawCardEffect());
    }
}
