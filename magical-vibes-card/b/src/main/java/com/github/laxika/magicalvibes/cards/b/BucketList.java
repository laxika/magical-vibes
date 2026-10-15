package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TrackCastSpellCardTypesEffect;
import java.util.Set;

@CardRegistration(set = "MB1", collectorNumber = "89")
public class BucketList extends Card {
    public BucketList() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new TrackCastSpellCardTypesEffect(Set.of(
                CardType.ARTIFACT, CardType.CREATURE, CardType.ENCHANTMENT, CardType.INSTANT, CardType.SORCERY)));
    }
}
