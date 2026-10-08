package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PopulateEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.condition.SourceIsOnBattlefield;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M3C", collectorNumber = "6")
@CardRegistration(set = "M3C", collectorNumber = "10")
@CardRegistration(set = "M3C", collectorNumber = "18")
@CardRegistration(set = "M3C", collectorNumber = "26")
@CardRegistration(set = "M3C", collectorNumber = "137")
public class CaythFamedMechanist extends Card {

    public CaythFamedMechanist() {
        // Fabricate's counter-or-Servo choice is made as the ability resolves
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, fabricateTrigger());
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_ENTER_BATTLEFIELD,
                fabricateTrigger(),
                GrantScope.OWN_CREATURES,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsTokenPredicate())
                ))
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption("Populate", new PopulateEffect()),
                        new ChooseOneEffect.ChooseOneOption("Proliferate", new ProliferateEffect())
                ))),
                "{2}, {T}: Choose one — Populate. — Proliferate."
        ).withModalChoiceAtActivation());
    }

    private CardEffect fabricateTrigger() {
        CardEffect servo = new CreateTokenEffect(1, "Servo", 1, 1, null,
                List.of(CardSubtype.SERVO), Set.of(), Set.of(CardType.ARTIFACT));
        return new ConditionalReplacementEffect(new SourceIsOnBattlefield(), servo,
                new ChooseOneAtResolutionEffect(fabricate()));
    }

    private ChooseOneEffect fabricate() {
        return new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Put a +1/+1 counter on this creature",
                        new PutCountersOnSourceEffect(1, 1, 1)
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Create a 1/1 colorless Servo artifact creature token",
                        new CreateTokenEffect(1, "Servo", 1, 1, null,
                                List.of(CardSubtype.SERVO), Set.of(), Set.of(CardType.ARTIFACT))
                )
        ));
    }
}
