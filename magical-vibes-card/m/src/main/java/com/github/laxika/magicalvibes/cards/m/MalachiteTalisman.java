package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ICE", collectorNumber = "328")
public class MalachiteTalisman extends Card {

    public MalachiteTalisman() {
        // Whenever a player casts a green spell, you may pay {3}. If you do, untap target permanent.
        target(TargetFilters.permanent()).addTrigger(Triggers.playerCastsSpell()
                        .withFilter(new CardColorPredicate(CardColor.GREEN)),
                UnlessPaysEffect.youMayPay("{3}", new UntapPermanentsEffect(Subjects.target())));
    }
}
