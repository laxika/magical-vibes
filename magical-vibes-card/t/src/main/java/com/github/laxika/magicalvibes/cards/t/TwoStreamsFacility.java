package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenPlayerModeEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesPlanarModeEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersWithChosenPlanarModePlayAdditionalLandEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "603")
public class TwoStreamsFacility extends Card {

    private static final String GREEN_ANCHOR = "Green anchor";
    private static final String RED_WATERFALL = "Red waterfall";
    private static final List<String> MODES = List.of(GREEN_ANCHOR, RED_WATERFALL);

    public TwoStreamsFacility() {
        addTrigger(Triggers.planeswalkToThis(),
                new EachPlayerChoosesPlanarModeEffect(MODES, false, false));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new EachPlayerChoosesPlanarModeEffect(MODES, true, false));
        addStaticEffect(new PlayersWithChosenPlanarModePlayAdditionalLandEffect(GREEN_ANCHOR));
        addStaticEffect(new BoostCreaturesOfChosenPlayerModeEffect(RED_WATERFALL, 2, 0, Set.of(Keyword.HASTE)));
        addTrigger(Triggers.chaosEnsues(),
                new EachPlayerChoosesPlanarModeEffect(MODES, false, true));
    }
}
