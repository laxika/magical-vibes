package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "277")
@CardRegistration(set = "FIN", collectorNumber = "355")
public class EdenSeatOfTheSanctum extends Card {

    public EdenSeatOfTheSanctum() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(
                        new MillEffect(2, Subjects.you()),
                        new MayEffect(
                                SequenceEffect.of(
                                        new SacrificePermanentsEffect(1, new PermanentIsSourceCardPredicate(), Subjects.you()),
                                        new ConditionalEffect(HappenedThisWay.sacrificed(), new QueueReflexiveAbilityEffect(
                                                ReturnCardFromGraveyardEffect.builder()
                                                .destination(GraveyardChoiceDestination.HAND)
                                                .filter(new CardAllOfPredicate(List.of(
                                                        new CardIsPermanentPredicate(),
                                                        new CardNotPredicate(new CardIsSelfPredicate())
                                                )))
                                                .targetGraveyard(true)
                                                .build()))),
                                "Sacrifice this land?")),
                "{5}, {T}: Mill two cards. Then you may sacrifice this land. When you do, return another target permanent card from your graveyard to your hand."
        ));
    }
}
