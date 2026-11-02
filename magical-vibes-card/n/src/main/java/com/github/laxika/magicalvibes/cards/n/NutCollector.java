package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ODY", collectorNumber = "259")
@CardRegistration(set = "DMR", collectorNumber = "171")
public class NutCollector extends Card {

    public NutCollector() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                new CreateTokenEffect("Squirrel", 1, 1, CardColor.GREEN, List.of(CardSubtype.SQUIRREL),
                        Set.of(), Set.of()),
                "Create a 1/1 green Squirrel creature token?"));

        addStaticEffect(new ConditionalEffect(
                new GraveyardCardThreshold(7, null),
                new StaticBoostEffect(2, 2, GrantScope.ALL_CREATURES,
                        new PermanentHasSubtypePredicate(CardSubtype.SQUIRREL))));
    }
}
