package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerSacrificesAttackingCreatureThenCreateTokensEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "PIP", collectorNumber = "160")
@CardRegistration(set = "PIP", collectorNumber = "449")
@CardRegistration(set = "PIP", collectorNumber = "688")
@CardRegistration(set = "PIP", collectorNumber = "977")
@CardRegistration(set = "40K", collectorNumber = "185")
public class EntrapmentManeuver extends Card {

    public EntrapmentManeuver() {
        CreateTokenEffect soldier = new CreateTokenEffect(
                "Soldier", 1, 1, CardColor.WHITE, List.of(CardSubtype.SOLDIER), Set.of(), Set.of());

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.SPELL,
                new TargetPlayerSacrificesAttackingCreatureThenCreateTokensEqualToToughnessEffect(soldier));
    }
}
