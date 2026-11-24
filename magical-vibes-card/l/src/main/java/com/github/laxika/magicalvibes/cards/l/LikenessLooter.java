package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfSubjectEffect;
import com.github.laxika.magicalvibes.model.effect.CopyExceptions;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "WOE", collectorNumber = "208")
@CardRegistration(set = "WOE", collectorNumber = "364")
public class LikenessLooter extends Card {

    public LikenessLooter() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DrawCardEffect(), new DiscardEffect(1, Subjects.you())),
                "{T}: Draw a card, then discard a card."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}",
                List.of(BecomeCopyOfSubjectEffect.of(Subjects.targetCard(new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE), new CardManaValueEqualsXPredicate())),
                                GraveyardSearchScope.CONTROLLERS_GRAVEYARD))
                        .keepingThisAbility()
                        .except(CopyExceptions.withKeywords(Keyword.FLYING))),
                "{X}: This creature becomes a copy of target creature card in your graveyard with mana value X, "
                        + "except it has flying and this ability. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
