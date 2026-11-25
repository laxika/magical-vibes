package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MB1", collectorNumber = "117")
public class JasconianIsle extends Card {

    public JasconianIsle() {
        addStaticEffect(new EntersTappedEffect());
        addStaticEffect(DoesntUntapEffect.self());
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), UnlessPaysEffect.youMayPay("{U}{U}", new UntapPermanentsEffect(Subjects.source()))
                .withPrompt("Pay {U}{U} to untap Jasconian Isle?"));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
    }
}
