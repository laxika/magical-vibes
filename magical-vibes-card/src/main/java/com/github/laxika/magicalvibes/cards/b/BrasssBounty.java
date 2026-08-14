package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "190")
public class BrasssBounty extends Card {

    public BrasssBounty() {
        PermanentCount landsYouControl = new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.ARTIFACT,
                landsYouControl,
                "Treasure", 0, 0, null, null,
                List.of(CardSubtype.TREASURE), Set.of(), Set.of(),
                false, false, Map.of(),
                List.of(new ActivatedAbility(
                        true, null,
                        List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect()),
                        "{T}, Sacrifice this artifact: Add one mana of any color.")),
                false, false, false, 0, Set.of()));
    }
}
