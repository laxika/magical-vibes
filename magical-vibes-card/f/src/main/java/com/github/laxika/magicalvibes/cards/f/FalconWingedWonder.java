package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "52")
public class FalconWingedWonder extends Card {

    public FalconWingedWonder() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CreateTokenEffect(
                CardType.CREATURE,
                1,
                "Redwing",
                1,
                1,
                CardColor.BLUE,
                null,
                List.of(CardSubtype.BIRD, CardSubtype.SCOUT),
                Set.of(Keyword.FLYING),
                Set.of(),
                false,
                false,
                List.of(LegacySlots.triggered(EffectSlot.ON_ATTACK, new SurveilEffect(1))),
                List.of(),
                false,
                false,
                true,
                0,
                Set.of()
        ));
    }
}
