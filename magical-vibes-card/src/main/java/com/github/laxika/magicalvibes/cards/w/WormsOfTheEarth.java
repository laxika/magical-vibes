package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardsCantEnterBattlefieldFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantPlayLandsIfPermanentCountEffect;
import com.github.laxika.magicalvibes.model.effect.WormsOfTheEarthEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.Set;

@CardRegistration(set = "DRK", collectorNumber = "56")
public class WormsOfTheEarth extends Card {

    public WormsOfTheEarth() {
        addEffect(EffectSlot.STATIC, new PlayersCantPlayLandsIfPermanentCountEffect(
                0, new PermanentTruePredicate()));
        addEffect(EffectSlot.STATIC, new CardsCantEnterBattlefieldFromZonesEffect(
                new CardTypePredicate(CardType.LAND), Set.of(Zone.GRAVEYARD, Zone.LIBRARY)));
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new WormsOfTheEarthEffect());
    }
}
