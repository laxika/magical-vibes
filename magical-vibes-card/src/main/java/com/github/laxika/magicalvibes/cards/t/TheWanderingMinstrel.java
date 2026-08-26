package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledPermanentsEnterUntappedEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "249")
public class TheWanderingMinstrel extends Card {

    private static final Set<CardColor> ALL_COLORS = Set.of(
            CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN);

    public TheWanderingMinstrel() {
        addEffect(EffectSlot.STATIC,
                new ControlledPermanentsEnterUntappedEffect(new PermanentIsLandPredicate()));

        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ConditionalEffect(
                new ControlsPermanentCount(5, new PermanentHasSubtypePredicate(CardSubtype.TOWN)),
                new CreateTokenEffect(
                        "Elemental", 2, 2, null, ALL_COLORS, List.of(CardSubtype.ELEMENTAL))));

        PermanentCount towns = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.TOWN), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}{U}{B}{R}{G}",
                List.of(new BoostAllOwnCreaturesEffect(
                        towns,
                        towns,
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()))),
                "{3}{W}{U}{B}{R}{G}: Other creatures you control get +X/+X until end of turn, where X is the number of Towns you control."
        ));
    }
}
