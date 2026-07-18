package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "318")
public class EbonyHorse extends Card {

    public EbonyHorse() {
        // {2}, {T}: Untap target attacking creature you control. Prevent all combat damage that would be
        // dealt to and dealt by that creature this turn. Same target for all three effects; both prevention
        // effects are combat-only (noncombat damage still lands).
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new UntapPermanentsEffect(TapUntapScope.TARGET),
                        PreventDamageEffect.allCombatToTargetCreatures(),
                        PreventDamageEffect.allCombatByTargetCreatures()),
                "{2}, {T}: Untap target attacking creature you control. Prevent all combat damage that would be dealt to and dealt by that creature this turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsAttackingPredicate(),
                                new PermanentControlledBySourceControllerPredicate())),
                        "Target must be an attacking creature you control")));
    }
}
