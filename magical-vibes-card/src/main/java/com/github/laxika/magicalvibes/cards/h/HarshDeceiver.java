package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.condition.TopCardOfLibraryType;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "11")
public class HarshDeceiver extends Card {

    public HarshDeceiver() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new LookAtTopCardsOfTargetLibraryEffect(1, TargetLibraryAction.LOOK_ONLY)),
                "{1}: Look at the top card of your library."));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new RevealTopCardOfLibraryEffect(LibraryOwner.CONTROLLER),
                        new ConditionalEffect(
                                new TopCardOfLibraryType(CardType.LAND),
                                SequenceEffect.of(
                                        new UntapPermanentsEffect(TapUntapScope.SOURCE_PERMANENT),
                                        new BoostSelfEffect(1, 1)))),
                "{2}: Reveal the top card of your library. If it's a land card, untap this creature "
                        + "and it gets +1/+1 until end of turn. Activate only once each turn.",
                1));
    }
}
