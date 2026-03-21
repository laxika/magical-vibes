package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "73")
public class VodalianArcanist extends Card {

    public VodalianArcanist() {
        // {T}: Add {C}. Spend this mana only to cast an instant or sorcery spell.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(ManaColor.COLORLESS, 1, Set.of(CardType.INSTANT, CardType.SORCERY))),
                "{T}: Add {C}. Spend this mana only to cast an instant or sorcery spell."
        ));
    }
}
