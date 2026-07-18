package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsToUntapSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "5ED", collectorNumber = "98")
@CardRegistration(set = "4ED", collectorNumber = "80")
public class Leviathan extends Card {

    public Leviathan() {
        PermanentHasSubtypePredicate islands = new PermanentHasSubtypePredicate(CardSubtype.ISLAND);

        // Enters tapped and doesn't untap during your untap step
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.STATIC, DoesntUntapEffect.self());

        // At the beginning of your upkeep, you may sacrifice two Islands. If you do, untap Leviathan.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new SacrificePermanentsToUntapSelfEffect(2, islands, "two Islands"),
                "You may sacrifice two Islands. If you do, untap this creature."
        ));

        // Leviathan can't attack unless you sacrifice two Islands (paid as attackers are declared):
        // the legality gate (control enough to pay) plus the actual sacrifice cost.
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new ControlsPermanentCount(2, islands), "you sacrifice two Islands"));
        addEffect(EffectSlot.STATIC, new CantAttackUnlessSacrificeEffect(2, islands, "two Islands"));
    }
}
