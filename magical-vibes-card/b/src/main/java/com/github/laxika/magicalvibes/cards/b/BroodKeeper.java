package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "M15", collectorNumber = "132")
public class BroodKeeper extends Card {

    public BroodKeeper() {
        // Whenever an Aura becomes attached to this creature, create a 2/2 red Dragon creature token
        // with flying. It has "{R}: This creature gets +1/+0 until end of turn."
        addEffect(EffectSlot.ON_AURA_ATTACHED_TO_SELF, new CreateTokenEffect(
                CardType.CREATURE, 1, "Dragon", 2, 2, CardColor.RED, null,
                List.of(CardSubtype.DRAGON), Set.of(Keyword.FLYING), Set.of(), false, false,
                Map.<EffectSlot, CardEffect>of(),
                List.of(new ActivatedAbility(false, "{R}", List.of(new BoostSelfEffect(1, 0)),
                        "{R}: This creature gets +1/+0 until end of turn.")),
                false, false, false, 0, Set.of()));
    }
}
