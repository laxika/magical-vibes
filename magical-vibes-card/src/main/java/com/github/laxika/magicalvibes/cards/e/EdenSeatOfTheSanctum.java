package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "277")
public class EdenSeatOfTheSanctum extends Card {

    public EdenSeatOfTheSanctum() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(
                        new MillEffect(2, MillRecipient.CONTROLLER),
                        new MayEffect(
                                new SacrificePermanentThenEffect(
                                        new PermanentIsSourceCardPredicate(),
                                        ReturnCardFromGraveyardEffect.builder()
                                                .destination(GraveyardChoiceDestination.HAND)
                                                .filter(new CardAllOfPredicate(List.of(
                                                        new CardIsPermanentPredicate(),
                                                        new CardNotPredicate(new CardIsSelfPredicate())
                                                )))
                                                .targetGraveyard(true)
                                                .build(),
                                        "this land"),
                                "Sacrifice this land?")),
                "{5}, {T}: Mill two cards. Then you may sacrifice this land. When you do, return another target permanent card from your graveyard to your hand."
        ));
    }
}
