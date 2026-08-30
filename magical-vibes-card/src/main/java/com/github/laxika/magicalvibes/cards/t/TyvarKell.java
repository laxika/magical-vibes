package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordsToCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "198")
public class TyvarKell extends Card {

    private static final String EMBLEM_TEXT =
            "Whenever you cast an Elf spell, it gains haste until end of turn and you draw two cards.";

    public TyvarKell() {
        PermanentHasSubtypePredicate elf = new PermanentHasSubtypePredicate(CardSubtype.ELF);
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                ManaAbilities.tapFor(ManaColor.BLACK), GrantScope.OWN_CREATURES, elf));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new UntapPermanentsEffect(TapUntapScope.TARGET, elf),
                        new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.TARGET)
                ),
                "+1: Put a +1/+1 counter on up to one target Elf. Untap it. It gains deathtouch until end of turn.",
                new PermanentPredicateTargetFilter(elf, "Target must be an Elf"),
                +1,
                null,
                null,
                List.of(),
                0,
                1
        ));

        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new CreateTokenEffect(
                        "Elf Warrior", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.ELF, CardSubtype.WARRIOR), Set.of(), Set.of()
                )),
                "0: Create a 1/1 green Elf Warrior creature token."
        ));

        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new CreateEmblemEffect(
                        List.of(new SpellCastTriggerEffect(
                                new CardSubtypePredicate(CardSubtype.ELF),
                                List.of(
                                        new GrantKeywordsToCastSpellEffect(Set.of(Keyword.HASTE)),
                                        new DrawCardEffect(2)
                                )
                        )),
                        EMBLEM_TEXT
                )),
                "−6: You get an emblem with \"" + EMBLEM_TEXT + "\"."
        ));
    }
}
