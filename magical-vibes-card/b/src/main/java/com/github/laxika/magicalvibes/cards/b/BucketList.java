package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.TrackCastSpellCardTypesEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.Set;

@CardRegistration(set = "MB1", collectorNumber = "89")
public class BucketList extends Card {
    public BucketList() {
        addTrigger(Triggers.youCastSpell(), new TrackCastSpellCardTypesEffect(Set.of(
                CardType.ARTIFACT, CardType.CREATURE, CardType.ENCHANTMENT, CardType.INSTANT, CardType.SORCERY)));
    }
}
