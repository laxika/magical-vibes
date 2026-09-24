package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "1598")
@CardRegistration(set = "C14", collectorNumber = "43")
public class FreyaliseLlanowarsFury extends Card {

    public FreyaliseLlanowarsFury() {
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new CreateTokenEffect(
                        CardType.CREATURE,
                        1,
                        "Elf Druid",
                        1,
                        1,
                        CardColor.GREEN,
                        null,
                        List.of(CardSubtype.ELF, CardSubtype.DRUID),
                        Set.of(),
                        Set.of(),
                        false,
                        false,
                        Map.of(),
                        List.of(ManaAbilities.tapFor(ManaColor.GREEN)),
                        false,
                        false,
                        false,
                        0,
                        Set.of()
                )),
                "+2: Create a 1/1 green Elf Druid creature token with \"{T}: Add {G}.\""
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new DestroyTargetPermanentEffect()),
                "−2: Destroy target artifact or enchantment.",
                new PermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsEnchantmentPredicate()
                        )),
                        "Target must be an artifact or enchantment"
                )
        ));

        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new DrawCardEffect(new PermanentCount(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentColorInPredicate(Set.of(CardColor.GREEN))
                        )),
                        CountScope.CONTROLLER
                ))),
                "−6: Draw a card for each green creature you control."
        ));
    }
}
