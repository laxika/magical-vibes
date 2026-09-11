package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayPlayFromGraveyardThisTurnEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOS", collectorNumber = "132")
public class TabletOfDiscovery extends Card {

    public TabletOfDiscovery() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillControllerAndMayPlayFromGraveyardThisTurnEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.RED)),
                "{T}: Add {R}."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(
                        ManaColor.RED,
                        2,
                        new ManaRestriction.SpellTypes(Set.of(CardType.INSTANT, CardType.SORCERY))
                )),
                "{T}: Add {R}{R}. Spend this mana only to cast instant and sorcery spells."
        ));
    }
}
