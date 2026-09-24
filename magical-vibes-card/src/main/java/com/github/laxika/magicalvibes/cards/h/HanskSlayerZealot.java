package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "144")
@CardRegistration(set = "SLX", collectorNumber = "22")
public class HanskSlayerZealot extends Card {

    public HanskSlayerZealot() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.UPKEEP_TRIGGERED, new CreateTokenForTargetPlayerEffect(
                new CreateTokenEffect(3, "Walker", 2, 2, CardColor.BLACK,
                        List.of(CardSubtype.ZOMBIE), Set.of(), Set.of())));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToTargetCreatureEffect(2)),
                "{T}: Hansk, Slayer Zealot deals 2 damage to target creature.",
                TargetFilters.creature()
        ));

        addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.ZOMBIE), new DrawCardEffect()));
    }
}
