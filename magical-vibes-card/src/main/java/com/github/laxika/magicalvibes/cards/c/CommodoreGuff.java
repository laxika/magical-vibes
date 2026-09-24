package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "CMM", collectorNumber = "706")
@CardRegistration(set = "CMM", collectorNumber = "781")
public class CommodoreGuff extends Card {

    public CommodoreGuff() {
        // At the beginning of your end step, put a loyalty counter on another target planeswalker
        // you control.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentControlledBySourceControllerPredicate(),
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                )),
                "Target must be another planeswalker you control"
        )).addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new PutCounterOnTargetPermanentEffect(CounterType.LOYALTY));

        // +1: Create a 1/1 red Wizard creature token with "{T}: Add {R}. Spend this mana only to
        // cast a planeswalker spell."
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new CreateTokenEffect(
                        CardType.CREATURE,
                        1,
                        "Wizard",
                        1,
                        1,
                        CardColor.RED,
                        null,
                        List.of(CardSubtype.WIZARD),
                        Set.of(),
                        Set.of(),
                        false,
                        false,
                        Map.of(),
                        List.of(new ActivatedAbility(
                                true,
                                null,
                                List.of(new AwardRestrictedManaEffect(
                                        ManaColor.RED,
                                        1,
                                        new ManaRestriction.SubtypeOrPlaneswalkerSpells()
                                )),
                                "{T}: Add {R}. Spend this mana only to cast a planeswalker spell."
                        )),
                        false,
                        false,
                        false,
                        0,
                        Set.of()
                )),
                "+1: Create a 1/1 red Wizard creature token with \"{T}: Add {R}. Spend this mana "
                        + "only to cast a planeswalker spell.\""
        ));

        // −3: You draw X cards and Commodore Guff deals X damage to each opponent, where X is the
        // number of planeswalkers you control.
        PermanentCount planeswalkersYouControl = new PermanentCount(
                new PermanentIsPlaneswalkerPredicate(), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(
                        new DrawCardEffect(planeswalkersYouControl),
                        new DealDamageToPlayersEffect(planeswalkersYouControl, DamageRecipient.EACH_OPPONENT)
                ),
                "−3: You draw X cards and Commodore Guff deals X damage to each opponent, where X is "
                        + "the number of planeswalkers you control."
        ));
    }
}
