package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "200")
public class EmbermageGoblin extends Card {

    public EmbermageGoblin() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchLibraryEffect(new CardNamedPredicate("Embermage Goblin")),
                "Search your library for a card named Embermage Goblin?"));
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: This creature deals 1 damage to any target."));
    }
}
