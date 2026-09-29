package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfDefenderControlsMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueAtMostSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControllerControlsPermanentCountAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "DMC", collectorNumber = "29")
@CardRegistration(set = "DMC", collectorNumber = "51")
public class AyeshaTanakaArmorer extends Card {

    public AyeshaTanakaArmorer() {
        addEffect(EffectSlot.ON_ATTACK,
                LookAtTopCardsEffect.mayPutUpToMatchingOntoBattlefieldTappedRestOnBottomRandom(
                        4,
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.ARTIFACT),
                                new CardManaValueAtMostSourcePowerPredicate())),
                        4,
                        false));
        addEffect(EffectSlot.STATIC, new CantBeBlockedIfDefenderControlsMatchingPermanentEffect(
                new PermanentNotPredicate(new PermanentControllerControlsPermanentCountAtMostPredicate(
                        2, new PermanentIsArtifactPredicate()))));
    }
}
