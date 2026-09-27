package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantHaveOrGainKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToOpponentCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;

import java.util.List;

@CardRegistration(set = "C14", collectorNumber = "59")
@CardRegistration(set = "SOC", collectorNumber = "361")
public class ArcaneLighthouse extends Card {

    public ArcaneLighthouse() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new RemoveKeywordEffect(Keyword.HEXPROOF, GrantScope.OPPONENT_CREATURES),
                        new RemoveKeywordEffect(Keyword.SHROUD, GrantScope.OPPONENT_CREATURES),
                        new GrantStaticEffectToOpponentCreaturesUntilEndOfTurnEffect(
                                new CantHaveOrGainKeywordEffect(Keyword.HEXPROOF)),
                        new GrantStaticEffectToOpponentCreaturesUntilEndOfTurnEffect(
                                new CantHaveOrGainKeywordEffect(Keyword.SHROUD))),
                "{1}, {T}: Until end of turn, creatures your opponents control lose hexproof and shroud "
                        + "and can't have hexproof or shroud."
        ));
    }
}
