package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "9ED", collectorNumber = "170")
@CardRegistration(set = "CHR", collectorNumber = "42")
@CardRegistration(set = "ATQ", collectorNumber = "21")
public class YawgmothDemon extends Card {

    public YawgmothDemon() {
        // At the beginning of your upkeep, you may sacrifice an artifact. If you don't,
        // tap this creature and it deals 2 damage to you.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                UnlessPaysEffect.unlessYouPay(new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "artifact", false), SequenceEffect.of(new TapPermanentsEffect(Subjects.source()), new DealDamageToPlayersEffect(2, Subjects.you()))));
    }
}
