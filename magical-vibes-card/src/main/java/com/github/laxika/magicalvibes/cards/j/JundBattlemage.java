package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALA", collectorNumber = "106")
public class JundBattlemage extends Card {

    public JundBattlemage() {
        // {B}, {T}: Target player loses 1 life.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER)),
                "{B}, {T}: Target player loses 1 life.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                )
        ));

        // {G}, {T}: Create a 1/1 green Saproling creature token.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(new CreateTokenEffect("Saproling", 1, 1,
                        CardColor.GREEN, List.of(CardSubtype.SAPROLING), Set.of(), Set.of())),
                "{G}, {T}: Create a 1/1 green Saproling creature token."
        ));
    }
}
