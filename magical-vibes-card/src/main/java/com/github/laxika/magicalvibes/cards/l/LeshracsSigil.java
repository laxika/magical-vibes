package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "144")
public class LeshracsSigil extends Card {

    public LeshracsSigil() {
        // Whenever an opponent casts a green spell, you may pay {B}{B}. If you do, look at that
        // player's hand and choose a card from it. The player discards that card.
        // (Collector stamps the casting opponent as non-targeting targetId = "that player".)
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.GREEN),
                        List.of(new MayPayManaEffect("{B}{B}",
                                new ChooseCardsFromTargetHandEffect(1, List.of(), HandChoiceDestination.DISCARD),
                                "Pay {B}{B} to look at that player's hand and discard a card?"))));

        // {B}{B}: Return this enchantment to its owner's hand.
        addActivatedAbility(new ActivatedAbility(false, "{B}{B}", List.of(ReturnToHandEffect.self()),
                "{B}{B}: Return Leshrac's Sigil to its owner's hand."));
    }
}
