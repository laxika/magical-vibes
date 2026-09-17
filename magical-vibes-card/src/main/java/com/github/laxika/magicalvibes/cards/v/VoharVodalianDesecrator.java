package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.AllowCastTargetCardFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "226")
public class VoharVodalianDesecrator extends Card {

    private static final CardAnyOfPredicate INSTANT_OR_SORCERY = new CardAnyOfPredicate(List.of(
            new CardTypePredicate(CardType.INSTANT),
            new CardTypePredicate(CardType.SORCERY)));

    public VoharVodalianDesecrator() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new DrawCardEffect(),
                        new DiscardCardThenEffect(
                                null,
                                SequenceEffect.of(
                                        new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
                                        new GainLifeEffect(1)),
                                "a card",
                                INSTANT_OR_SORCERY)),
                "{T}: Draw a card, then discard a card. If you discarded an instant or sorcery card "
                        + "this way, each opponent loses 1 life and you gain 1 life."));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new SacrificeSelfCost(),
                        new AllowCastTargetCardFromGraveyardThisTurnEffect(
                                INSTANT_OR_SORCERY,
                                GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                                true)),
                "{2}, Sacrifice Vohar: You may cast target instant or sorcery card from your graveyard "
                        + "this turn. If that spell would be put into your graveyard, exile it instead.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
