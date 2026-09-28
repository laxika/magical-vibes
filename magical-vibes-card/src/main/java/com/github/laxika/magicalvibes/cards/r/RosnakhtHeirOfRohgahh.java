package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMC", collectorNumber = "25")
@CardRegistration(set = "DMC", collectorNumber = "75")
public class RosnakhtHeirOfRohgahh extends Card {

    public RosnakhtHeirOfRohgahh() {
        // Whenever you cast a spell that targets Rosnakht, create a 0/1 red Kobold creature token.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new CreateTokenEffect("Kobolds of Kher Keep", 0, 1, CardColor.RED,
                        List.of(CardSubtype.KOBOLD), Set.of(), Set.of())),
                new StackEntryTargetsSourcePredicate()
        ));
    }
}
