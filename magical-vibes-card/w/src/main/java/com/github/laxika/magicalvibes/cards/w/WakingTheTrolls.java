package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "234")
public class WakingTheTrolls extends Card {

    public WakingTheTrolls() {
        addTrigger(Triggers.chapter(1),
                new DestroyTargetPermanentEffect(new PermanentIsLandPredicate()));
        setSagaChapterTargetFilter(1, Set.of(TargetFilters.land()));

        addTrigger(Triggers.chapter(2), ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.LAND))
                .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                .targetGraveyard(true)
                .build());

        PermanentCount yourLands = new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER);
        PermanentCount targetLands = new PermanentCount(new PermanentIsLandPredicate(), CountScope.TARGET_PLAYER);
        addTrigger(Triggers.chapter(3), new CreateTokenEffect(
                new Max(new Fixed(0), new Sum(yourLands, new Scaled(targetLands, -1))),
                "Troll Warrior", 4, 4, CardColor.GREEN,
                List.of(CardSubtype.TROLL, CardSubtype.WARRIOR), Set.of(Keyword.TRAMPLE), Set.of()));
        setSagaChapterTargetFilter(3, Set.of(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Target must be an opponent")));
    }
}
