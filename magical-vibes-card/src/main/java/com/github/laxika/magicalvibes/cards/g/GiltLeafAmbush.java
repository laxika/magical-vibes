package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "214")
public class GiltLeafAmbush extends Card {

    public GiltLeafAmbush() {
        // Create two 1/1 green Elf Warrior creature tokens.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(2, "Elf Warrior", 1, 1, CardColor.GREEN,
                List.of(CardSubtype.ELF, CardSubtype.WARRIOR), Set.of(), Set.of()));

        // Clash with an opponent. If you win, those creatures gain deathtouch until end of turn.
        addEffect(EffectSlot.SPELL, new ClashEffect(
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.TOKENS_CREATED_THIS_RESOLUTION)));
    }
}
