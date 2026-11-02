package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromHandAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "BIG", collectorNumber = "25")
public class NexusOfBecoming extends Card {

    private static final CardAnyOfPredicate ARTIFACT_OR_CREATURE = new CardAnyOfPredicate(List.of(
            new CardTypePredicate(CardType.ARTIFACT),
            new CardTypePredicate(CardType.CREATURE)
    ));

    public NexusOfBecoming() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), SequenceEffect.of(
                new DrawCardEffect(),
                new MayEffect(
                        new ExileCardFromHandAndCreateTokenCopyEffect(
                                ARTIFACT_OR_CREATURE,
                                new CreateTokenCopyOfTargetPermanentEffect(
                                        List.of(CardSubtype.GOLEM),
                                        Set.of(CardType.ARTIFACT, CardType.CREATURE),
                                        3, 3, Map.of())),
                        "Exile an artifact or creature card from your hand?")));
    }
}
