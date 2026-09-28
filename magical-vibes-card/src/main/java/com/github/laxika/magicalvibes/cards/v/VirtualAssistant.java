package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TeamworkSpellCastTriggerEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "643")
public class VirtualAssistant extends Card {

    public VirtualAssistant() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new TeamworkSpellCastTriggerEffect(List.of(
                new CreateTokenEffect("Robot", 1, 1, null,
                        List.of(CardSubtype.ROBOT, CardSubtype.HERO),
                        Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT)))));
    }
}
