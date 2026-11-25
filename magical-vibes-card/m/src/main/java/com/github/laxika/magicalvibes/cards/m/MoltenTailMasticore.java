package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "177")
public class MoltenTailMasticore extends Card {

    public MoltenTailMasticore() {
        // At the beginning of your upkeep, sacrifice Molten-Tail Masticore unless you discard a card.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(new DiscardEffect(1, Subjects.you()),
                "Discard a card? If you don't, sacrifice this creature.",
                new SacrificeSelfEffect()));

        // {4}, Exile a creature card from your graveyard: Molten-Tail Masticore deals 4 damage to any target.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(new ExileCardFromGraveyardCost(CardType.CREATURE), DealDamageEffect.toAnyTarget(4)),
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
