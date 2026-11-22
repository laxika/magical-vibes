package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "VOC", collectorNumber = "13")
@CardRegistration(set = "VOC", collectorNumber = "51")
public class HauntingImitation extends Card {

    private static final CardPredicate CREATURE = new CardTypePredicate(CardType.CREATURE);

    public HauntingImitation() {
        addSpellEffect(SequenceEffect.of(
                new RevealTopCardOfLibraryEffect(Subjects.eachPlayer()),
                CreateTokenCopyOfCardEffect.of(Subjects.cardsThisWay(StepKind.REVEAL, CREATURE),
                        new CreateTokenCopyOfTargetPermanentEffect(
                                List.of(CardSubtype.SPIRIT), Set.of(), 1, 1, Map.of(), null,
                                Set.of(Keyword.FLYING))),
                new ConditionalEffect(new NotCondition(HappenedThisWay.toACard(StepKind.REVEAL, CREATURE)),
                        ReturnToHandEffect.selfSpell())));
    }
}
