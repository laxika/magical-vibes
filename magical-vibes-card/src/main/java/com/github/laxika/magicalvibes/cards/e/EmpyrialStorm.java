package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellForEachCommanderCastEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C18", collectorNumber = "2")
public class EmpyrialStorm extends Card {

    public EmpyrialStorm() {
        // When you cast this spell, copy it for each time you've cast your commander from the
        // command zone this game.
        addEffect(EffectSlot.ON_SELF_CAST, new CopyThisSpellForEachCommanderCastEffect(false));

        // Create a 4/4 white Angel creature token with flying.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Angel", 4, 4, CardColor.WHITE, List.of(CardSubtype.ANGEL),
                Set.of(Keyword.FLYING), Set.of()));
    }
}
