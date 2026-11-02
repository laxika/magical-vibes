package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "41")
@CardRegistration(set = "WTH", collectorNumber = "24")
@CardRegistration(set = "SLD", collectorNumber = "1893")
@CardRegistration(set = "MB2", collectorNumber = "152")
public class Serenity extends Card {

    public Serenity() {
        // At the beginning of your upkeep, destroy all artifacts and enchantments. They can't be regenerated.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new DestroyAllPermanentsEffect(new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsEnchantmentPredicate())), true));
    }
}
