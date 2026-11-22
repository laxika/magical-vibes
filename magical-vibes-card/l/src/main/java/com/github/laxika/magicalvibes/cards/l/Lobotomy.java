package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "267")
@CardRegistration(set = "INV", collectorNumber = "255")
public class Lobotomy extends Card {

    public Lobotomy() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addSpellEffect(new ChooseCardsFromTargetHandEffect(
                1, List.of(), new CardNotPredicate(CardPredicateUtils.basicLand()),
                HandChoiceDestination.KEEP_IN_HAND))
                .addSpellEffect(ExileCardsWithSameNameEffect.all(Subjects.cardsThisWay(StepKind.CHOOSE))
                        .searching(Subjects.targetPlayerDeclaredElsewhere(),
                                Zone.GRAVEYARD, Zone.HAND, Zone.LIBRARY));
    }
}
