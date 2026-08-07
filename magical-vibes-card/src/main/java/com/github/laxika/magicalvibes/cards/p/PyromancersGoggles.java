package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterNextRedInstantSorceryCopyEffect;

import java.util.List;

/**
 * Pyromancer's Goggles — Legendary Artifact.
 * {T}: Add {R}. When that mana is spent to cast a red instant or sorcery spell,
 * copy that spell and you may choose new targets for the copy.
 */
@CardRegistration(set = "ORI", collectorNumber = "236")
public class PyromancersGoggles extends Card {

    public PyromancersGoggles() {
        // {T}: Add {R}. When that mana is spent to cast a red instant or sorcery spell,
        // copy that spell and you may choose new targets for the copy.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new AwardManaEffect(ManaColor.RED), new RegisterNextRedInstantSorceryCopyEffect()),
                "{T}: Add {R}. When that mana is spent to cast a red instant or sorcery spell, copy that spell and you may choose new targets for the copy."
        ));
    }
}
