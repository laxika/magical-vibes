package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfNextPlayerNonlandPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerDirection;
import com.github.laxika.magicalvibes.model.effect.PutControllerCardFromHandOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1421")
public class AminatouTheFateshifter extends Card {

    public AminatouTheFateshifter() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new DrawCardEffect(1), new PutControllerCardFromHandOnTopOfLibraryEffect()),
                "+1: Draw a card, then put a card from your hand on top of your library."
        ));

        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(FlickerEffect.flickerTargetUnderYourControl()),
                "−1: Exile another target permanent you own, then return it to the battlefield under your control.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentOwnedBySourceControllerPredicate(),
                                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
                        )),
                        "Target must be another permanent you own"
                )
        ));

        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Left",
                                new GainControlOfNextPlayerNonlandPermanentsEffect(PlayerDirection.LEFT)),
                        new ChooseOneEffect.ChooseOneOption(
                                "Right",
                                new GainControlOfNextPlayerNonlandPermanentsEffect(PlayerDirection.RIGHT))
                ))),
                "−6: Choose left or right. Each player gains control of all nonland permanents other than "
                        + "Aminatou controlled by the next player in the chosen direction."
        ));
    }
}
