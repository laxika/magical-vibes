package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "230")
@CardRegistration(set = "BRC", collectorNumber = "127")
public class OniCultAnvil extends Card {

    public OniCultAnvil() {
        CardEffect createConstruct = new CreateTokenEffect(
                "Construct", 1, 1, null, List.of(CardSubtype.CONSTRUCT), Set.of(), Set.of(CardType.ARTIFACT));

        // One ability (CR 603.3) whose limit covers both conditions: another artifact you control, and this one.
        addTriggeredAbility(new TriggeredAbility(List.of(
                new TriggerCondition(Triggers.leavesBattlefield(Triggers.ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL)
                        .onlyDuringYourTurn()),
                new TriggerCondition(Triggers.leavesBattlefield(Triggers.ARTIFACT, ObjectRelation.THIS)
                        .onlyDuringYourTurn())),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                List.of(new EffectRegistration(createConstruct, TriggerMode.NORMAL))));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false),
                        new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT),
                        new GainLifeEffect(1)
                ),
                "{T}, Sacrifice an artifact: This artifact deals 1 damage to each opponent. You gain 1 life."
        ));
    }
}
