package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "53")
public class Ether extends Card {

    public Ether() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new ExileSelfCost(),
                        new AwardManaEffect(ManaColor.BLUE),
                        new CopyNextInstantOrSorceryCastThisTurnEffect()),
                "{T}, Exile this artifact: Add {U}. When you next cast an instant or sorcery spell this turn, copy that spell. You may choose new targets for the copy."
        ));
    }
}
