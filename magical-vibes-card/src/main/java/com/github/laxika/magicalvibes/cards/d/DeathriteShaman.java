package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "213")
public class DeathriteShaman extends Card {

    public DeathriteShaman() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD,
                                new CardTypePredicate(CardType.LAND)),
                        new AwardAnyColorManaEffect()
                ),
                "{T}: Exile target land card from a graveyard. Add one mana of any color."
        ));

        CardPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(
                        new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD,
                                instantOrSorcery),
                        new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT)
                ),
                "{B}, {T}: Exile target instant or sorcery card from a graveyard. Each opponent loses 2 life."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(
                        new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD,
                                new CardTypePredicate(CardType.CREATURE)),
                        new GainLifeEffect(2)
                ),
                "{G}, {T}: Exile target creature card from a graveyard. You gain 2 life."
        ));
    }
}
