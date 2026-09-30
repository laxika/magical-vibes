package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyControllerAllSpellsAndAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "M3C", collectorNumber = "4")
@CardRegistration(set = "M3C", collectorNumber = "16")
@CardRegistration(set = "M3C", collectorNumber = "24")
@CardRegistration(set = "M3C", collectorNumber = "143")
@CardRegistration(set = "M3C", collectorNumber = "147")
@CardRegistration(set = "M3C", collectorNumber = "151")
public class UlalekFusedAtrocity extends Card {

    public UlalekFusedAtrocity() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardSubtypePredicate(CardSubtype.ELDRAZI),
                List.of(new MayPayManaEffect(
                        "{C}{C}",
                        new CopyControllerAllSpellsAndAbilitiesEffect(),
                        "Pay {C}{C} to copy all spells and abilities?"))));
    }
}
