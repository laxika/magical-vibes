package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopiesOfChosenDistinctControlledTokensEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "203")
public class BattleForBretagard extends Card {

    public BattleForBretagard() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new CreateTokenEffect(
                1, "Human Warrior", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.HUMAN, CardSubtype.WARRIOR), Set.of(), Set.of()
        ));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new CreateTokenEffect(
                1, "Elf Warrior", 1, 1, CardColor.GREEN,
                List.of(CardSubtype.ELF, CardSubtype.WARRIOR), Set.of(), Set.of()
        ));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new CreateTokenCopiesOfChosenDistinctControlledTokensEffect());
    }
}
