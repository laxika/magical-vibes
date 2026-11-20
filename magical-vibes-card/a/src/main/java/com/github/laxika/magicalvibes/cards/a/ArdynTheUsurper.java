package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "89")
@CardRegistration(set = "FIN", collectorNumber = "315")
@CardRegistration(set = "FIN", collectorNumber = "379")
@CardRegistration(set = "FIN", collectorNumber = "444")
@CardRegistration(set = "FIN", collectorNumber = "524")
public class ArdynTheUsurper extends Card {

    public ArdynTheUsurper() {
        addStaticEffect(new GrantKeywordEffect(
                Set.of(Keyword.MENACE, Keyword.LIFELINK, Keyword.HASTE),
                Subjects.creaturesYouControl(),
                new PermanentHasSubtypePredicate(CardSubtype.DEMON)));

        target(0, 1).addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                SequenceEffect.of(
                        ExileGraveyardCardsEffect.upToOneTargetFromAnyGraveyard(
                                new CardTypePredicate(CardType.CREATURE)),
                        CreateTokenCopyOfCardEffect.of(Subjects.cardsThisWay(StepKind.EXILE),
                                        new CreateTokenCopyOfTargetPermanentEffect(List.of(), Set.of(), 5, 5,
                                                Map.of(), false, false, false, false, false, false, CardColor.BLACK,
                                                Set.of()))
                                .withCreatureTypesInstead(List.of(CardSubtype.DEMON))));
    }
}
