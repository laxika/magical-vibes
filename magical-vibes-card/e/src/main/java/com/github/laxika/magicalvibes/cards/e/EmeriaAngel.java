package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZEN", collectorNumber = "11")
@CardRegistration(set = "IMA", collectorNumber = "20")
@CardRegistration(set = "MOC", collectorNumber = "183")
@CardRegistration(set = "TDC", collectorNumber = "114")
@CardRegistration(set = "ONC", collectorNumber = "65")
@CardRegistration(set = "SCD", collectorNumber = "21")
@CardRegistration(set = "ZNC", collectorNumber = "15")
public class EmeriaAngel extends Card {

    public EmeriaAngel() {
        // Landfall - Whenever a land you control enters, you may create a 1/1 white Bird creature token with flying.
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), new MayEffect(
                new CreateTokenEffect("Bird", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of()),
                "Create a 1/1 white Bird creature token with flying?"));
    }
}
