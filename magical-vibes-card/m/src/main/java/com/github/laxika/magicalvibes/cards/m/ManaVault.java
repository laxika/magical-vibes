package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5ED", collectorNumber = "388")
@CardRegistration(set = "4ED", collectorNumber = "334")
@CardRegistration(set = "SUM", collectorNumber = "263")
@CardRegistration(set = "3ED", collectorNumber = "263")
@CardRegistration(set = "VMA", collectorNumber = "274")
@CardRegistration(set = "MPS", collectorNumber = "17")
@CardRegistration(set = "SLD", collectorNumber = "796")
@CardRegistration(set = "UMA", collectorNumber = "229")
@CardRegistration(set = "2X2", collectorNumber = "308")
@CardRegistration(set = "ME4", collectorNumber = "214")
@CardRegistration(set = "2ED", collectorNumber = "260")
@CardRegistration(set = "LEA", collectorNumber = "259")
@CardRegistration(set = "LEB", collectorNumber = "260")
public class ManaVault extends Card {

    public ManaVault() {
        // This artifact doesn't untap during your untap step.
        addStaticEffect(DoesntUntapEffect.self());

        // At the beginning of your upkeep, you may pay {4}. If you do, untap this artifact.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayPayManaEffect(
                "{4}",
                new UntapPermanentsEffect(TapUntapScope.SELF),
                "Pay {4} to untap Mana Vault?"));

        // At the beginning of your draw step, if this artifact is tapped, it deals 1 damage to you.
        addTrigger(Triggers.drawStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceIsTapped()),
                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER));

        // {T}: Add {C}{C}{C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS, 3));
    }
}
