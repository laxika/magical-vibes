package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOM", collectorNumber = "189")
public class OriginSpellbomb extends Card {

    public OriginSpellbomb() {
        // {1}, {T}, Sacrifice Origin Spellbomb: Create a 1/1 colorless Myr artifact creature token.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new SacrificeSelfCost(), new CreateCreatureTokenEffect(
                        "Myr", 1, 1, null,
                        List.of(CardSubtype.MYR), Set.of(), Set.of(CardType.ARTIFACT))),
                "{1}, {T}, Sacrifice Origin Spellbomb: Create a 1/1 colorless Myr artifact creature token."
        ));

        // When Origin Spellbomb is put into a graveyard from the battlefield, you may pay {W}. If you do, draw a card.
        addEffect(EffectSlot.ON_DEATH, new MayPayManaEffect("{W}", new DrawCardEffect(1), "Pay {W} to draw a card?"));
    }
}
