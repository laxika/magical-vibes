package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerUnlessControllerTakesDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "YMID", collectorNumber = "43")
public class TibaltWickedTormentor extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Chained Brute",
            "Charmbreaker Devils",
            "Festival Crasher",
            "Forge Devil",
            "Frenzied Devils",
            "Havoc Jester",
            "Hellrider",
            "Hobblefiend",
            "Pitchburn Devils",
            "Sin Prodder",
            "Spiteful Prankster",
            "Tibalt's Rager",
            "Torch Fiend",
            "Brimstone Vandal",
            "Devil's Play");

    public TibaltWickedTormentor() {
        // +1: Add {R}{R}. Draft a card from Tibalt, Wicked Tormentor's spellbook, then exile it.
        // Until end of turn, you may cast that card.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new AwardManaEffect(ManaColor.RED, 2),
                        new DraftCardFromSpellbookEffect(SPELLBOOK, true)),
                "+1: Add {R}{R}. Draft a card from Tibalt, Wicked Tormentor's spellbook, then exile it. "
                        + "Until end of turn, you may cast that card."
        ));

        // +1: Tibalt deals 4 damage to target creature or planeswalker unless its controller has
        // Tibalt deal 4 damage to them. If they do, you may discard a card. If you do, draw a card.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new DealDamageToTargetCreatureOrPlaneswalkerUnlessControllerTakesDamageEffect(
                        4, 4, new MayEffect(new DiscardAndDrawCardEffect(),
                        "Discard a card to draw a card?"))),
                "+1: Tibalt, Wicked Tormentor deals 4 damage to target creature or planeswalker unless "
                        + "its controller has Tibalt deal 4 damage to them. If they do, you may discard a "
                        + "card. If you do, draw a card."
        ));

        Map<EffectSlot, com.github.laxika.magicalvibes.model.effect.CardEffect> devilTokenEffects =
                Map.of(EffectSlot.ON_DEATH, new DealDamageToAnyTargetEffect(1));
        addActivatedAbility(ActivatedAbility.variableLoyaltyAbility(
                List.of(new CreateTokenEffect(new XValue(), "Devil", 1, 1, CardColor.RED,
                        List.of(CardSubtype.DEVIL), Set.of(), Set.of()).withTokenEffects(devilTokenEffects)),
                "−X: Create X 1/1 red Devil creature tokens with \"When this creature dies, it deals "
                        + "1 damage to any target.\"",
                null
        ));
    }
}
