package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaWithInstantSorceryCopyEffect;

import java.util.List;

/**
 * Primal Wellspring — back face of Primal Amulet // Primal Wellspring.
 * Land.
 * (Transforms from Primal Amulet.)
 * {T}: Add one mana of any color. When that mana is spent to cast an instant or sorcery spell,
 * copy that spell and you may choose new targets for the copy.
 */
public class PrimalWellspring extends Card {

    public PrimalWellspring() {
        // {T}: Add one mana of any color. When that mana is spent to cast an instant
        // or sorcery spell, copy that spell and you may choose new targets for the copy.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new AwardAnyColorManaWithInstantSorceryCopyEffect()),
                "{T}: Add one mana of any color. When that mana is spent to cast an instant or sorcery spell, copy that spell and you may choose new targets for the copy."
        ));
    }
}
