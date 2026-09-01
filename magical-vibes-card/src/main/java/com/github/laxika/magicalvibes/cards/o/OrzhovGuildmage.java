package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "147")
public class OrzhovGuildmage extends Card {

    public OrzhovGuildmage() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(new TargetPlayerGainsLifeEffect(1)),
                "{2}{W}: Target player gains 1 life.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                )
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(new LoseLifeEffect(1, LoseLifeRecipient.EACH_PLAYER)),
                "{2}{B}: Each player loses 1 life."
        ));
    }
}
