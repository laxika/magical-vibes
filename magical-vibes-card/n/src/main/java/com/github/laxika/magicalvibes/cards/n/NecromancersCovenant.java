package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ARB", collectorNumber = "82")
@CardRegistration(set = "C15", collectorNumber = "229")
public class NecromancersCovenant extends Card {

    public NecromancersCovenant() {
        // When this enchantment enters, exile all creature cards from target player's graveyard,
        // then create a 2/2 black Zombie creature token for each card exiled this way.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addTrigger(Triggers.enters(ObjectRelation.THIS),
                SequenceEffect.of(
                        new ExileGraveyardCardsEffect(0, GraveyardExileScope.TARGET_PLAYER_ALL_MATCHING,
                                new CardTypePredicate(CardType.CREATURE)),
                        new CreateTokenEffect(new AmountThisWay(StepKind.EXILE), "Zombie", 2, 2, CardColor.BLACK,
                                List.of(CardSubtype.ZOMBIE), Set.of(), Set.of())));

        // Zombies you control have lifelink.
        addStaticEffect(new GrantKeywordEffect(Keyword.LIFELINK, Subjects.creaturesYouControl(),
                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE)));
    }
}
