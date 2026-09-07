package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardsCantEnterBattlefieldFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCastSpellsFromZonesEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "222")
public class KunorosHoundOfAthreos extends Card {

    public KunorosHoundOfAthreos() {
        addEffect(EffectSlot.STATIC, new CardsCantEnterBattlefieldFromZonesEffect(
                new CardTypePredicate(CardType.CREATURE), Set.of(Zone.GRAVEYARD)));
        addEffect(EffectSlot.STATIC, new PlayersCantCastSpellsFromZonesEffect(Set.of(Zone.GRAVEYARD)));
    }
}
