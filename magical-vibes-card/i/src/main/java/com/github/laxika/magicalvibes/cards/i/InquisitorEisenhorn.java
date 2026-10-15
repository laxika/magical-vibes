package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RevealFirstDrawInstantOrSorceryCreateTokenEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "127")
public class InquisitorEisenhorn extends Card {

    public InquisitorEisenhorn() {
        CreateTokenEffect cherubael = new CreateTokenEffect(
                CardType.CREATURE, 1, "Cherubael", 4, 4, CardColor.BLACK, null,
                List.of(CardSubtype.DEMON), Set.of(Keyword.FLYING), Set.of(),
                false, false, Map.of(), List.of(), false, false, true, 0, Set.of(),
                Set.of(CardSupertype.LEGENDARY));

        addEffect(EffectSlot.ON_CONTROLLER_DRAWS,
                new RevealFirstDrawInstantOrSorceryCreateTokenEffect(cherubael));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                CreateTokenEffect.ofClueToken(new EventValue()));
    }
}
