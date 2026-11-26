package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "17")
@CardRegistration(set = "ME2", collectorNumber = "45")
public class DeepSpawn extends Card {

    public DeepSpawn() {
        // At the beginning of your upkeep, sacrifice this creature unless you mill two cards.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), UnlessPaysEffect.unlessYouPay(
                new MillControllerCost(2),
                new SacrificeSelfEffect()));

        // {U}: This creature gains shroud until end of turn and doesn't untap during your next
        // untap step. Tap this creature.
        addActivatedAbility(new ActivatedAbility(false, "{U}",
                List.of(new GrantKeywordEffect(Keyword.SHROUD, Subjects.source()),
                        new SkipNextUntapEffect(Subjects.source(), 1, false, false, true),
                        new TapPermanentsEffect(Subjects.source())),
                "{U}: This creature gains shroud until end of turn and doesn't untap during your next untap step. Tap this creature."));
    }
}
