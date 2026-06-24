package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardsCantEnterBattlefieldFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCastSpellsFromZonesEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.Set;

@CardRegistration(set = "DKA", collectorNumber = "149")
public class GrafdiggersCage extends Card {

    public GrafdiggersCage() {
        // Creature cards in graveyards and libraries can't enter the battlefield.
        addEffect(EffectSlot.STATIC, new CardsCantEnterBattlefieldFromZonesEffect(
                new CardTypePredicate(CardType.CREATURE), Set.of(Zone.GRAVEYARD, Zone.LIBRARY)));

        // Players can't cast spells from graveyards or libraries.
        addEffect(EffectSlot.STATIC, new PlayersCantCastSpellsFromZonesEffect(
                Set.of(Zone.GRAVEYARD, Zone.LIBRARY)));
    }
}
