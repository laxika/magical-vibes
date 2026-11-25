package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "179")
public class GoblinWarWagon extends Card {

    public GoblinWarWagon() {
        addStaticEffect(DoesntUntapEffect.self());
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), UnlessPaysEffect.youMayPay("{2}", new UntapPermanentsEffect(Subjects.source()))
                .withPrompt("Pay {2} to untap Goblin War Wagon?"));
    }
}
