package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "198")
public class BruseTarlRovingRancher extends Card {

    public BruseTarlRovingRancher() {
        addStaticEffect(new GrantKeywordEffect(Keyword.DOUBLE_STRIKE,
                Subjects.creaturesYouControl(), new PermanentHasSubtypePredicate(CardSubtype.OX)));

        CardTypePredicate land = new CardTypePredicate(CardType.LAND);
        var topCardEffect = SequenceEffect.of(
                ExileTopCardOfOwnLibraryEffect.of(Subjects.you()),
                new ConditionalEffect(HappenedThisWay.toACard(StepKind.EXILE, land),
                        new CreateTokenEffect("Ox", 2, 2, CardColor.WHITE, List.of(CardSubtype.OX),
                                Set.of(), Set.of())),
                AllowPlayCardsFromExileEffect.untilEndOfYourNextTurn(Subjects.cardsThisWay(StepKind.EXILE,
                        ExileCardsFromTopUntilEffect.NONLAND)));
        addTrigger(Triggers.enters(ObjectRelation.THIS), topCardEffect);
        addTrigger(Triggers.attacks(), topCardEffect);
    }
}
