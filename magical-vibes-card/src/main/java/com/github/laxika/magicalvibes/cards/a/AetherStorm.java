package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantCastSpellTypeEffect;
import com.github.laxika.magicalvibes.model.effect.DestroySourcePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "5ED", collectorNumber = "70")
public class AetherStorm extends Card {

    public AetherStorm() {
        // Creature spells can't be cast (symmetric — no player may cast them).
        addEffect(EffectSlot.STATIC, new CantCastSpellTypeEffect(Set.of(CardType.CREATURE), true));

        // Pay 4 life: Destroy this enchantment. It can't be regenerated. Any player may activate this ability.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new PayLifeCost(4), new DestroySourcePermanentEffect(true)),
                "Pay 4 life: Destroy Aether Storm. It can't be regenerated. Any player may activate this ability."
        ).withActivatableByAnyPlayer());
    }
}
