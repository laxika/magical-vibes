package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentCastCostEffect;

import java.util.List;
import java.util.Set;

public class AuraOfSilence extends Card {

    public AuraOfSilence() {
        super("Aura of Silence", CardType.ENCHANTMENT, "{1}{W}{W}", CardColor.WHITE);

        setCardText("Artifact and enchantment spells your opponents cast cost {2} more to cast.\nSacrifice Aura of Silence: Destroy target artifact or enchantment.");
        addEffect(EffectSlot.STATIC, new IncreaseOpponentCastCostEffect(Set.of(CardType.ARTIFACT, CardType.ENCHANTMENT), 2));
        addEffect(EffectSlot.ON_SACRIFICE, new DestroyTargetPermanentEffect(Set.of(CardType.ARTIFACT, CardType.ENCHANTMENT)));
    }
}
