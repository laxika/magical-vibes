package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensAttackingEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "TLA", collectorNumber = "100")
public class FireNavyTrebuchet extends Card {

    public FireNavyTrebuchet() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new CreateTokensAttackingEffect(
                1,
                new CreateTokenEffect(
                        CardType.CREATURE, 1, "Ballistic Boulder", 2, 1,
                        null, null, List.of(CardSubtype.CONSTRUCT),
                        Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT),
                        true, false, Map.of(), List.of(), false, false, false, 0, Set.of()),
                true));
    }
}
