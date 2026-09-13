package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "230")
public class OniCultAnvil extends Card {

    public OniCultAnvil() {
        CardEffect createConstruct = new CreateTokenEffect(
                "Construct", 1, 1, null, List.of(CardSubtype.CONSTRUCT), Set.of(), Set.of(CardType.ARTIFACT));

        addEffect(EffectSlot.ON_ALLY_PERMANENT_LEAVES_BATTLEFIELD_DURING_CONTROLLER_TURN,
                new OncePerTurnTriggerEffect(createConstruct));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new ConditionalEffect(new ControllerTurn(), createConstruct));

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
