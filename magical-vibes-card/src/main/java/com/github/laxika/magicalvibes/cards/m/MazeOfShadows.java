package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "319")
public class MazeOfShadows extends Card {

    public MazeOfShadows() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {T}: Untap target attacking creature with shadow. Prevent all combat damage that would be
        // dealt to and dealt by that creature this turn. Both prevention effects are combat-only.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET),
                        PreventDamageEffect.allCombatToTargetCreatures(),
                        PreventDamageEffect.allCombatByTargetCreatures()),
                "{T}: Untap target attacking creature with shadow. Prevent all combat damage that would be dealt to and dealt by that creature this turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsAttackingPredicate(),
                                new PermanentHasKeywordPredicate(Keyword.SHADOW))),
                        "Target must be an attacking creature with shadow")
        ));
    }
}
