package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "212")
public class TheComingOfGalactus extends Card {

    public TheComingOfGalactus() {
        addTrigger(Triggers.chapter(1), new DestroyTargetPermanentEffect(
                new PermanentNotPredicate(new PermanentIsLandPredicate())));
        addTrigger(Triggers.chapter(2), new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT));
        addTrigger(Triggers.chapter(3), new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT));

        CreateTokenEffect galactus = new CreateTokenEffect(
                CardType.CREATURE, 1, "Galactus", 16, 16,
                CardColor.BLACK, Set.of(), List.of(CardSubtype.ELDER, CardSubtype.ALIEN),
                Set.of(Keyword.FLYING, Keyword.TRAMPLE), Set.of(), false, false,
                List.of(new TriggeredAbility(Triggers.attacks(),
                        new DestroyTargetPermanentEffect(new PermanentIsLandPredicate()))),
                List.of(), false, false, true, 0, Set.of())
                .withTokenTargetFilter(TargetFilters.land());
        addTrigger(Triggers.chapter(4), galactus);
    }
}
