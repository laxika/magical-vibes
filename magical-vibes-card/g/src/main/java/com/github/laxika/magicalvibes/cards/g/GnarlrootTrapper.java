package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "100")
public class GnarlrootTrapper extends Card {

    public GnarlrootTrapper() {
        // {T}, Pay 1 life: Add {G}. Spend this mana only to cast an Elf creature spell.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PayLifeCost(1), new AwardRestrictedManaEffect(
                        ManaColor.GREEN, 1, new ManaRestriction.SubtypeCreatureSpells(CardSubtype.ELF))),
                "{T}, Pay 1 life: Add {G}. Spend this mana only to cast an Elf creature spell."
        ));

        // {T}: Target attacking Elf you control gains deathtouch until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.TARGET)),
                "{T}: Target attacking Elf you control gains deathtouch until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.ELF),
                                new PermanentIsAttackingPredicate(),
                                new PermanentControlledBySourceControllerPredicate()
                        )),
                        "Target must be an attacking Elf you control"
                )
        ));
    }
}
