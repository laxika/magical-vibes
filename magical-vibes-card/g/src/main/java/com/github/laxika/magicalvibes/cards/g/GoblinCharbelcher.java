package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "176")
@CardRegistration(set = "DDT", collectorNumber = "57")
@CardRegistration(set = "EMA", collectorNumber = "222")
@CardRegistration(set = "BRR", collectorNumber = "18")
@CardRegistration(set = "MB2", collectorNumber = "221")
public class GoblinCharbelcher extends Card {

    public GoblinCharbelcher() {
        var landCard = new CardTypePredicate(CardType.LAND);
        var mountain = new CardSubtypePredicate(CardSubtype.MOUNTAIN);
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new RevealCardsFromTopUntilEffect(landCard),
                        new ConditionalEffect(HappenedThisWay.toACard(StepKind.REVEAL, mountain),
                                DealDamageEffect.toAnyTarget(new Scaled(AmountThisWay.revealedCards(new CardNotPredicate(landCard)), 2))),
                        new ConditionalEffect(new NotCondition(HappenedThisWay.toACard(StepKind.REVEAL, mountain)),
                                DealDamageEffect.toAnyTarget(AmountThisWay.revealedCards(new CardNotPredicate(landCard)))),
                        PutCardsIntoLibraryEffect.onBottom(Subjects.revealedThisWay())),
                "{3}, {T}: Reveal cards from the top of your library until you reveal a land card."
                        + " This artifact deals damage equal to the number of nonland cards revealed this way to any target."
                        + " If the revealed land card was a Mountain, this artifact deals double that damage instead."
                        + " Put the revealed cards on the bottom of your library in any order."
        ));
    }
}
