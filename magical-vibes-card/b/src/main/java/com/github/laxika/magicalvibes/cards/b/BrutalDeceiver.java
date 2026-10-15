package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.TopCardOfLibraryType;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "161")
public class BrutalDeceiver extends Card {

    public BrutalDeceiver() {
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
                                        new BoostSelfEffect(1, 0),
                                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)))),
                "{2}: Reveal the top card of your library. If it's a land card, this creature gets "
                        + "+1/+0 and gains first strike until end of turn. Activate only once each turn.",
                1));
    }
}
