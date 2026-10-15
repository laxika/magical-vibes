package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "177")
public class MoltenTailMasticore extends Card {

    public MoltenTailMasticore() {
        // At the beginning of your upkeep, sacrifice Molten-Tail Masticore unless you discard a card.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SacrificeUnlessDiscardCardTypeEffect(null));

        // {4}, Exile a creature card from your graveyard: Molten-Tail Masticore deals 4 damage to any target.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(new ExileCardFromGraveyardCost(CardType.CREATURE), new DealDamageToAnyTargetEffect(4)),
                "{4}, Exile a creature card from your graveyard: Molten-Tail Masticore deals 4 damage to any target."
        ));

        // {2}: Regenerate Molten-Tail Masticore.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new RegenerateEffect()),
                "{2}: Regenerate Molten-Tail Masticore."
        ));
    }
}
