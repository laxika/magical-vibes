package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.HighestOpponentLifeTotal;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesOneEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerLifeExactlyPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "144")
@CardRegistration(set = "WHO", collectorNumber = "427")
@CardRegistration(set = "WHO", collectorNumber = "543")
@CardRegistration(set = "WHO", collectorNumber = "749")
@CardRegistration(set = "WHO", collectorNumber = "1018")
@CardRegistration(set = "WHO", collectorNumber = "1134")
public class TheMasterGallifreysEnd extends Card {

    public static final String LOSE_LIFE_OPTION = "They lose 4 life";
    public static final String CREATE_TOKEN_OPTION = "You create a token that's a copy of that card";

    public TheMasterGallifreysEnd() {
        addTrigger(Triggers.nontokenCreatureDies(new PermanentIsArtifactPredicate(), ObjectRelation.YOU_CONTROL),
                new MayEffect(SequenceEffect.of(
                        ExileGraveyardCardsEffect.cards(Subjects.triggeringCards()),
                        new ConditionalEffect(HappenedThisWay.exiled(), SequenceEffect.of(
                                new ChoosePlayerEffect(Subjects.matching(Subjects.eachOpponent(),
                                        new PlayerLifeExactlyPredicate(new HighestOpponentLifeTotal())),
                                        "an opponent with the most life among your opponents"),
                                TargetPlayerChoosesOneEffect.villainousChoiceFacedBy(
                                        Subjects.playersThisWay(StepKind.CHOOSE), List.of(
                                                new ChooseOneEffect.ChooseOneOption(LOSE_LIFE_OPTION,
                                                        new LoseLifeEffect(4,
                                                                Subjects.targetPlayerDeclaredElsewhere())),
                                                new ChooseOneEffect.ChooseOneOption(CREATE_TOKEN_OPTION,
                                                        CreateTokenCopyOfCardEffect.of(
                                                                Subjects.cardsThisWay(StepKind.EXILE),
                                                                new CreateTokenCopyOfTargetPermanentEffect()))))))),
                        "Exile that creature and make an opponent face a villainous choice?"));
    }
}
