package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.BlackChocobo;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "201")
public class SidequestRaiseAChocobo extends Card {

    public SidequestRaiseAChocobo() {
        setBackFaceCard(new BlackChocobo());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, birdToken());
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new ConditionalEffect(
                        new ControlsPermanentCount(4, new PermanentHasSubtypePredicate(CardSubtype.BIRD)),
                        new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "BlackChocobo";
    }

    private static CreateTokenEffect birdToken() {
        Map<EffectSlot, CardEffect> tokenEffects = Map.of(
                EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, new BoostSelfEffect(1, 0));
        return new CreateTokenEffect(
                CardType.CREATURE, 1, "Bird", 2, 2,
                CardColor.GREEN, null, List.of(CardSubtype.BIRD), Set.of(), Set.of(),
                false, false, tokenEffects, List.of(), false, false, false, 0, Set.of());
    }
}
