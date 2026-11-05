package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.SourceIntensity;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfReturnAtNextUpkeepWithHasteEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.IntensifySourceCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YOTJ", collectorNumber = "26")
public class TeysaOfTheGhostCouncil extends Card {

    public TeysaOfTheGhostCouncil() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new CreateTokenEffect(1, "Spirit", 1, 1, CardColor.WHITE,
                        Set.of(CardColor.WHITE, CardColor.BLACK), List.of(CardSubtype.SPIRIT),
                        Set.of(Keyword.FLYING), Set.of()),
                new IntensifySourceCardEffect(1));

        addStaticEffect(new DynamicStaticBoostEffect(
                new SourceIntensity(), new Fixed(0), GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SPIRIT)));

        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new MayEffect(new ExileSelfReturnAtNextUpkeepWithHasteEffect(),
                        "Exile Teysa of the Ghost Council? It returns at the beginning of your next upkeep with haste."));
    }
}
