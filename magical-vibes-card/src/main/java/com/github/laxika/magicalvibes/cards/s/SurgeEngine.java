package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SetSelfBasePowerToughnessIndefinitelyEffect;
import com.github.laxika.magicalvibes.model.effect.SetSelfCantBeBlockedIndefinitelyEffect;
import com.github.laxika.magicalvibes.model.effect.SetSelfColorIndefinitelyEffect;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SelfHasKeyword;
import com.github.laxika.magicalvibes.model.condition.SourceHasColor;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "81")
public class SurgeEngine extends Card {

    public SurgeEngine() {
        addActivatedAbility(new ActivatedAbility(false, "{U}",
                List.of(
                        new RemoveKeywordEffect(Keyword.DEFENDER, GrantScope.SELF, EffectDuration.PERMANENT),
                        new SetSelfCantBeBlockedIndefinitelyEffect()),
                "{U}: This creature loses defender and gains \"This creature can't be blocked.\""));
        addActivatedAbility(new ActivatedAbility(false, "{2}{U}",
                List.of(
                        new SetSelfColorIndefinitelyEffect(CardColor.BLUE),
                        new SetSelfBasePowerToughnessIndefinitelyEffect(5, 4)),
                "{2}{U}: This creature becomes blue and has base power and toughness 5/4. Activate only if this creature doesn't have defender.")
                .withActivationCondition(new NotCondition(new SelfHasKeyword(Keyword.DEFENDER)),
                        "This creature must not have defender"));
        addActivatedAbility(new ActivatedAbility(false, "{4}{U}{U}",
                List.of(new DrawCardEffect(3)),
                "{4}{U}{U}: Draw three cards. Activate only if this creature is blue and only once.")
                .withActivationCondition(new SourceHasColor(CardColor.BLUE), "This creature must be blue")
                .withMaxActivationsPerGame(1));
    }
}
