package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutCardFromHandToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "MSH", collectorNumber = "241")
public class WorldsWithinWorlds extends Card {

    public WorldsWithinWorlds() {
        addSpellEffect(new ExileAllPermanentsEffect(new PermanentIsCreaturePredicate()));
        addSpellEffect(new EachPlayerMayPutCardFromHandToBattlefieldEffect(new CardTypePredicate(CardType.CREATURE),
                "creature", false, false, false, true));
        addSpellEffect(new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.EXILE)));
        addSpellEffect(new ExileSpellEffect());
    }
}
