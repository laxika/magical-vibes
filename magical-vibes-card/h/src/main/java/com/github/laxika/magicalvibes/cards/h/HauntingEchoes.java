package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ExileCardsWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "M10", collectorNumber = "98")
@CardRegistration(set = "M11", collectorNumber = "99")
@CardRegistration(set = "ODY", collectorNumber = "142")
public class HauntingEchoes extends Card {

    public HauntingEchoes() {
        addSpellEffect(new ExileGraveyardCardsEffect(0, GraveyardExileScope.TARGET_PLAYER_ALL_MATCHING,
                new CardNotPredicate(CardPredicateUtils.basicLand())));
        addSpellEffect(ExileCardsWithSameNameEffect.all(Subjects.cardsThisWay(StepKind.EXILE))
                .searching(Subjects.targetPlayerDeclaredElsewhere(), Zone.LIBRARY));
    }
}
