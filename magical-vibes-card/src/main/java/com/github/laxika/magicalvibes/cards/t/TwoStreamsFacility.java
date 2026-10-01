package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenPlayerModeEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesPlanarModeEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersWithChosenPlanarModePlayAdditionalLandEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "603")
public class TwoStreamsFacility extends Card {

    private static final String GREEN_ANCHOR = "Green anchor";
    private static final String RED_WATERFALL = "Red waterfall";
    private static final List<String> MODES = List.of(GREEN_ANCHOR, RED_WATERFALL);

    public TwoStreamsFacility() {
        addEffect(EffectSlot.PLANESWALK_TO_TRIGGERED,
                new EachPlayerChoosesPlanarModeEffect(MODES, false, false));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new EachPlayerChoosesPlanarModeEffect(MODES, true, false));
        addEffect(EffectSlot.STATIC,
                new PlayersWithChosenPlanarModePlayAdditionalLandEffect(GREEN_ANCHOR));
        addEffect(EffectSlot.STATIC,
                new BoostCreaturesOfChosenPlayerModeEffect(RED_WATERFALL, 2, 0, Set.of(Keyword.HASTE)));
        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new EachPlayerChoosesPlanarModeEffect(MODES, false, true));
    }
}
