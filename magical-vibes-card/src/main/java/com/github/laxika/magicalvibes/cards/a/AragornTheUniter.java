package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOC", collectorNumber = "29")
@CardRegistration(set = "HOC", collectorNumber = "69")
public class AragornTheUniter extends Card {

    public AragornTheUniter() {
        // Whenever you cast a white spell, create a 1/1 white Human Soldier creature token.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardColorPredicate(CardColor.WHITE),
                List.of(new CreateTokenEffect("Human Soldier", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER), Set.of(), Set.of()))
        ));

        // Whenever you cast a blue spell, scry 2.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardColorPredicate(CardColor.BLUE),
                List.of(new ScryEffect(2))
        ));

        // Whenever you cast a red spell, Aragorn deals 3 damage to target opponent.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardColorPredicate(CardColor.RED),
                List.of(new DealDamageToPlayersEffect(3, DamageRecipient.TARGET_PLAYER)),
                null,
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent")
        ));

        // Whenever you cast a green spell, target creature gets +4/+4 until end of turn.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardColorPredicate(CardColor.GREEN),
                List.of(new BoostTargetCreatureEffect(4, 4)),
                null,
                TargetFilters.creature()
        ));
    }
}
