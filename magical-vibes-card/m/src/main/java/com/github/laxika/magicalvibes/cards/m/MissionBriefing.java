package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.AllowCastTargetCardFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "44")
@CardRegistration(set = "MKC", collectorNumber = "110")
public class MissionBriefing extends Card {

    private static final CardAnyOfPredicate INSTANT_OR_SORCERY = new CardAnyOfPredicate(List.of(
            new CardTypePredicate(CardType.INSTANT),
            new CardTypePredicate(CardType.SORCERY)));

    public MissionBriefing() {
        addSpellEffect(new SurveilEffect(2));
        addSpellEffect(ChooseCardEffect.oneOf(Subjects.cardsInYourGraveyard(INSTANT_OR_SORCERY),
                "an instant or sorcery card in your graveyard"));
        addSpellEffect(AllowCastTargetCardFromGraveyardThisTurnEffect.of(
                Subjects.cardsThisWay(StepKind.CHOOSE), true));
    }
}
