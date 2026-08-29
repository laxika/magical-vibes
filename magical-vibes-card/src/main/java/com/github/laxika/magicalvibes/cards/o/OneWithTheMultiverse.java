package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromTopOfLibraryEffect;

import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "59")
public class OneWithTheMultiverse extends Card {

    public OneWithTheMultiverse() {
        addEffect(EffectSlot.STATIC, new LookAtTopCardOfOwnLibraryEffect());
        addEffect(EffectSlot.STATIC, new PlayLandsFromTopOfLibraryEffect());
        addEffect(EffectSlot.STATIC, new AllowCastFromTopOfLibraryEffect(Set.of(
                CardType.CREATURE,
                CardType.ENCHANTMENT,
                CardType.SORCERY,
                CardType.INSTANT,
                CardType.ARTIFACT,
                CardType.PLANESWALKER,
                CardType.BATTLE,
                CardType.KINDRED
        )));
        addEffect(EffectSlot.STATIC, AlternativeCostForSpellsEffect.onceDuringControllerTurn(null));
    }
}
