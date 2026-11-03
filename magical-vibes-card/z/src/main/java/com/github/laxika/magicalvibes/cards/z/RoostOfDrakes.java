package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.KickedSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZNR", collectorNumber = "74")
public class RoostOfDrakes extends Card {

    private static final CreateTokenEffect DRAKE_TOKEN = new CreateTokenEffect(
            "Drake", 2, 2, CardColor.BLUE, List.of(CardSubtype.DRAKE), Set.of(Keyword.FLYING), Set.of());

    public RoostOfDrakes() {
        addStaticEffect(new KickerEffect("{2}{U}"));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new Kicked()),
                DRAKE_TOKEN);
        addTrigger(Triggers.youCastSpell(),
                new KickedSpellCastTriggerEffect(List.of(DRAKE_TOKEN)));
    }
}
