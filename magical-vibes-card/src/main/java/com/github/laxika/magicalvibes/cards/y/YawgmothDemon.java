package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "170")
public class YawgmothDemon extends Card {

    public YawgmothDemon() {
        // At the beginning of your upkeep, you may sacrifice an artifact. If you don't,
        // tap this creature and it deals 2 damage to you.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "Sacrifice an artifact"),
                        List.of(new TapPermanentsEffect(TapUntapScope.SELF),
                                new DealDamageToPlayersEffect(2, DamageRecipient.CONTROLLER)),
                        true));
    }
}
