package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;

@CardRegistration(set = "MIR", collectorNumber = "233")
public class PreferredSelection extends Card {

    public PreferredSelection() {
        // At the beginning of your upkeep, look at the top two cards of your library. You may
        // sacrifice this enchantment and pay {2}{G}{G}. If you do, put one of those cards into
        // your hand. If you don't, put one of those cards on the bottom of your library.
        //
        // The peek comes first so the pay/decline choice is informed (no target = own library).
        // Either branch then shows the same two cards: paying picks one for hand and leaves the
        // other on top, declining picks the one that stays on top and bottoms the other — for two
        // cards that is exactly "put one of those cards on the bottom".
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new LookAtTopCardsOfTargetLibraryEffect(2, TargetLibraryAction.LOOK_ONLY),
                new MayPayManaEffect(
                        "{2}{G}{G}",
                        SequenceEffect.of(
                                new SacrificeSelfEffect(),
                                LookAtTopCardsEffect.chooseOneToHandRestOnTop(2)),
                        "sacrifice it and pay {2}{G}{G} to put one of those cards into your hand?",
                        LookAtTopCardsEffect.putOneOnTopRestOnBottom(2))));
    }
}
