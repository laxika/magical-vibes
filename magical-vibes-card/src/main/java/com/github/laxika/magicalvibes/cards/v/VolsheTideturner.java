package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "77")
public class VolsheTideturner extends Card {

    public VolsheTideturner() {
        // {T}: Add {U}. Spend this mana only to cast an instant or sorcery spell or a kicked spell.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(
                        ManaColor.BLUE,
                        1,
                        new ManaRestriction.KickedOrInstantSorceryCosts())),
                "{T}: Add {U}. Spend this mana only to cast an instant or sorcery spell or a kicked spell."
        ));
    }
}
