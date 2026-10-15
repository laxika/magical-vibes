package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMC", collectorNumber = "41")
@CardRegistration(set = "DMC", collectorNumber = "63")
public class RohgahhKherKeepOverlord extends Card {

    public RohgahhKherKeepOverlord() {
        // Other Kobolds you control get +2/+2.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.KOBOLD)));

        // Whenever you cast a Kobold spell, you may pay {2}. If you do, create a 4/4 red Dragon
        // creature token with flying.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardSubtypePredicate(CardSubtype.KOBOLD),
                List.of(new MayPayManaEffect("{2}",
                        new CreateTokenEffect("Dragon", 4, 4, CardColor.RED,
                                List.of(CardSubtype.DRAGON), Set.of(Keyword.FLYING), Set.of()),
                        "Pay {2} to create a 4/4 red Dragon creature token with flying?"))));

        // Whenever you cast a Dragon spell, create a 0/1 red Kobold creature token named Kobolds
        // of Kher Keep.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardSubtypePredicate(CardSubtype.DRAGON),
                List.of(new CreateTokenEffect("Kobolds of Kher Keep", 0, 1, CardColor.RED,
                        List.of(CardSubtype.KOBOLD), Set.of(), Set.of()))));
    }
}
