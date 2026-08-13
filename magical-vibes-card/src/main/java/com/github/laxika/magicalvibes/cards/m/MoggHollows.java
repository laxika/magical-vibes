package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "320")
@CardRegistration(set = "TPR", collectorNumber = "239")
public class MoggHollows extends Card {

    public MoggHollows() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {T}: Add {R} or {G}. This land doesn't untap during your next untap step.
        // "Add {R} or {G}" is modeled as two separate mana abilities (same idiom as Adarkar Wastes).
        // Both are mana abilities (CR 605.1a), so the skip-untap rider resolves inline in
        // ActivatedAbilityExecutionService.doResolveManaAbility.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SkipNextUntapEffect(TapUntapScope.SELF), new AwardManaEffect(ManaColor.RED)),
                "{T}: Add {R}. Mogg Hollows doesn't untap during your next untap step."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SkipNextUntapEffect(TapUntapScope.SELF), new AwardManaEffect(ManaColor.GREEN)),
                "{T}: Add {G}. Mogg Hollows doesn't untap during your next untap step."
        ));
    }
}
