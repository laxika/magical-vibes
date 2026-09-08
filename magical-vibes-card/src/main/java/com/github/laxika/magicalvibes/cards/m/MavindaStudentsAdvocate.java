package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.GrantTargetGraveyardCardCastEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "21")
public class MavindaStudentsAdvocate extends Card {

    public MavindaStudentsAdvocate() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{0}",
                List.of(new GrantTargetGraveyardCardCastEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))),
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                        true,
                        8)),
                "{0}: You may cast target instant or sorcery card from your graveyard this turn. "
                        + "If that spell doesn't target a creature you control, it costs {8} more to cast this way. "
                        + "If that spell would be put into your graveyard, exile it instead. "
                        + "Activate only once each turn.",
                1));
    }
}
