package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "187")
@CardRegistration(set = "A25", collectorNumber = "226")
@CardRegistration(set = "C13", collectorNumber = "249")
@CardRegistration(set = "BRC", collectorNumber = "152")
@CardRegistration(set = "C17", collectorNumber = "218")
@CardRegistration(set = "SCD", collectorNumber = "270")
public class NihilSpellbomb extends Card {

    public NihilSpellbomb() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(),
                        new ExileGraveyardCardsEffect(GraveyardExileScope.TARGET_PLAYER_ENTIRE)),
                "{T}, Sacrifice Nihil Spellbomb: Exile target player's graveyard."
        ));

        addEffect(EffectSlot.ON_DEATH, new MayPayManaEffect("{B}", new DrawCardEffect(1), "Pay {B} to draw a card?"));
    }
}
