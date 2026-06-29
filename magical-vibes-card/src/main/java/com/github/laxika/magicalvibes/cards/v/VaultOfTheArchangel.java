package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DKA", collectorNumber = "158")
public class VaultOfTheArchangel extends Card {

    public VaultOfTheArchangel() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));

        // {2}{W}{B}, {T}: Creatures you control gain deathtouch and lifelink until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{W}{B}",
                List.of(new GrantKeywordEffect(
                        Set.of(Keyword.DEATHTOUCH, Keyword.LIFELINK),
                        GrantScope.OWN_CREATURES
                )),
                "{2}{W}{B}, {T}: Creatures you control gain deathtouch and lifelink until end of turn."
        ));
    }
}
