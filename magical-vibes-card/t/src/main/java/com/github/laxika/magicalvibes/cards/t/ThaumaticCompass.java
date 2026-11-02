package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SpiresOfOrazca;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "249")
public class ThaumaticCompass extends Card {

    public ThaumaticCompass() {
        setBackFaceCard(new SpiresOfOrazca());

        // {3}, {T}: Search your library for a basic land card, reveal it, put it into your hand, then shuffle.
        addActivatedAbility(new ActivatedAbility(
                true, "{3}",
                List.of(new SearchLibraryEffect(CardPredicateUtils.basicLand())),
                "{3}, {T}: Search your library for a basic land card, reveal it, put it into your hand, then shuffle."
        ));

        // At the beginning of your end step, if you control seven or more lands, transform this artifact.
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new ConditionalEffect(new ControlsPermanentCount(7, new PermanentIsLandPredicate()), new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "SpiresOfOrazca";
    }
}
