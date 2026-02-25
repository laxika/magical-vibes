package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandToHandEffect;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "165")
public class HorizonSpellbomb extends Card {

    public HorizonSpellbomb() {
        // {2}, {T}, Sacrifice Horizon Spellbomb: Search your library for a basic land card, reveal it, put it into your hand, then shuffle.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new SacrificeSelfCost(), new SearchLibraryForBasicLandToHandEffect()),
                "{2}, {T}, Sacrifice Horizon Spellbomb: Search your library for a basic land card, reveal it, put it into your hand, then shuffle."
        ));

        // When Horizon Spellbomb is put into a graveyard from the battlefield, you may pay {G}. If you do, draw a card.
        addEffect(EffectSlot.ON_DEATH, new MayPayManaEffect("{G}", new DrawCardEffect(1), "Pay {G} to draw a card?"));
    }
}
