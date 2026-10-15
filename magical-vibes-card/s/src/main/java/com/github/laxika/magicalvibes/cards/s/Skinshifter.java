package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SourceBecomesSubtypeUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "M12", collectorNumber = "195")
public class Skinshifter extends Card {

    private static final String RHINO_MODE =
            "Until end of turn, this creature becomes a Rhino with base power and toughness 4/4 and gains trample.";
    private static final String BIRD_MODE =
            "Until end of turn, this creature becomes a Bird with base power and toughness 2/2 and gains flying.";
    private static final String PLANT_MODE =
            "Until end of turn, this creature becomes a Plant with base power and toughness 0/8.";

    public Skinshifter() {
        // "{G}: Choose one. Activate only once each turn." Each mode replaces the source's creature
        // types (SourceBecomesSubtypeUntilEndOfTurnEffect) and sets its base P/T until end of turn;
        // none of the modes targets, so the mode is picked as the ability resolves.
        addActivatedAbility(new ActivatedAbility(false, "{G}", List.of(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(RHINO_MODE, List.of(
                        new SourceBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.RHINO),
                        new SetBasePowerToughnessEffect(4, 4, GrantScope.SELF),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF))),
                new ChooseOneEffect.ChooseOneOption(BIRD_MODE, List.of(
                        new SourceBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.BIRD),
                        new SetBasePowerToughnessEffect(2, 2, GrantScope.SELF),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF))),
                new ChooseOneEffect.ChooseOneOption(PLANT_MODE, List.of(
                        new SourceBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.PLANT),
                        new SetBasePowerToughnessEffect(0, 8, GrantScope.SELF)))))),
                "{G}: Choose one. Activate only once each turn.", 1));
    }
}
