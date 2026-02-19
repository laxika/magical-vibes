package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesBySubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardOfSubtypeFromGraveyardToHandEffect;

import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "155")
public class LordOfTheUndead extends Card {

    public LordOfTheUndead() {
        addEffect(EffectSlot.STATIC, new BoostCreaturesBySubtypeEffect(Set.of(CardSubtype.ZOMBIE), 1, 1, Set.of()));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{B}",
                List.of(new ReturnCardOfSubtypeFromGraveyardToHandEffect(CardSubtype.ZOMBIE)),
                false,
                "{1}{B}, {T}: Return target Zombie card from your graveyard to your hand."
        ));
    }
}
