package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsMoreLands;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "12")
@CardRegistration(set = "MSC", collectorNumber = "302")
public class DoraMilajeElite extends Card {

    private static final CreateTokenEffect VIBRANIUM_TOKEN = new CreateTokenEffect(
            CardType.ARTIFACT,
            1,
            "Vibranium",
            0,
            0,
            null,
            null,
            List.of(),
            Set.of(Keyword.INDESTRUCTIBLE),
            Set.of(),
            false,
            true,
            Map.of(),
            List.of(new ActivatedAbility(
                    true,
                    null,
                    List.of(new AwardRestrictedManaEffect(
                            ManaColor.COLORLESS, 1, new ManaRestriction.Powerstone())),
                    "{T}: Add {C}. This mana can't be spent to cast a nonartifact spell."
            )),
            false,
            false,
            false,
            0,
            Set.of()
    );

    public DoraMilajeElite() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new OpponentControlsMoreLands(), VIBRANIUM_TOKEN));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new GrantKeywordEffect(
                                Keyword.INDESTRUCTIBLE,
                                GrantScope.OWN_PERMANENTS,
                                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY))
                ),
                "Sacrifice this creature: Legendary permanents you control gain indestructible until end of turn."
        ));
    }
}
