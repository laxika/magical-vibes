package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeColorlessEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MB1", collectorNumber = "102")
public class SlivdraziMonstrosity extends Card {

    private static final PermanentHasSubtypePredicate ELDRAZI =
            new PermanentHasSubtypePredicate(CardSubtype.ELDRAZI);
    private static final PermanentHasSubtypePredicate SLIVER =
            new PermanentHasSubtypePredicate(CardSubtype.SLIVER);

    private static final CreateTokenEffect ELDRAZI_SLIVER = new CreateTokenEffect(
            CardType.CREATURE,
            1,
            "Eldrazi Sliver",
            1,
            1,
            null,
            null,
            List.of(CardSubtype.ELDRAZI, CardSubtype.SLIVER),
            Set.of(),
            Set.of(),
            false,
            false,
            Map.of(),
            List.of(new ActivatedAbility(
                    false,
                    null,
                    List.of(new SacrificeSelfCost(), new AwardManaEffect(ManaColor.COLORLESS)),
                    "Sacrifice this creature: Add {C}."
            )),
            false,
            false,
            false,
            0,
            Set.of());

    public SlivdraziMonstrosity() {
        addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(
                CardSubtype.SLIVER, GrantScope.ALL_OWN_CREATURES, false, ELDRAZI));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.DEVOID, GrantScope.ALL_OWN_CREATURES, SLIVER));
        addEffect(EffectSlot.STATIC, new BecomeColorlessEffect(
                GrantScope.ALL_OWN_CREATURES, SLIVER));
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_ATTACK,
                new SacrificePermanentsEffect(
                        1, new PermanentTruePredicate(), SacrificeRecipient.DEFENDING_PLAYER),
                GrantScope.ALL_OWN_CREATURES,
                SLIVER));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(ELDRAZI_SLIVER),
                "{3}: Create a 1/1 colorless Eldrazi Sliver creature token. It has \"Sacrifice this creature: Add {C}.\""
        ));
    }
}
