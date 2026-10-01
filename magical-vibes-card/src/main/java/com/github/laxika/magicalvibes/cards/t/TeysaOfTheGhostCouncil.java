package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
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

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YOTJ", collectorNumber = "26")
public class TeysaOfTheGhostCouncil extends Card {

    public TeysaOfTheGhostCouncil() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect(1, "Spirit", 1, 1, CardColor.WHITE,
                        Set.of(CardColor.WHITE, CardColor.BLACK), List.of(CardSubtype.SPIRIT),
                        Set.of(Keyword.FLYING), Set.of()));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new IntensifySourceCardEffect(1));

        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                new SourceIntensity(), new Fixed(0), GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SPIRIT)));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new MayEffect(new ExileSelfReturnAtNextUpkeepWithHasteEffect(),
                        "Exile Teysa of the Ghost Council? It returns at the beginning of your next upkeep with haste."));
    }
}
