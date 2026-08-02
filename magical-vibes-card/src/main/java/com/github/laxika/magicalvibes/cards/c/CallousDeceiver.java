package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;
import com.github.laxika.magicalvibes.model.condition.TopCardOfLibraryType;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "53")
public class CallousDeceiver extends Card {

    public CallousDeceiver() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new LookAtTopCardsOfTargetLibraryEffect(1, TargetLibraryAction.LOOK_ONLY)),
                "{1}: Look at the top card of your library."));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new RevealTopCardOfOwnLibraryEffect(),
                        new ConditionalEffect(
                                new TopCardOfLibraryType(CardType.LAND),
                                SequenceEffect.of(
                                        new BoostSelfEffect(1, 0),
                                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)))),
                "{2}: Reveal the top card of your library. If it's a land card, this creature gets +1/+0 "
                        + "and gains flying until end of turn. Activate only once each turn.",
                1));
    }
}
