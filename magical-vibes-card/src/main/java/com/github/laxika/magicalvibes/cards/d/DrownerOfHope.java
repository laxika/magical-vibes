package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "BFZ", collectorNumber = "57")
public class DrownerOfHope extends Card {

    private static final CreateTokenEffect ELDRAZI_SCION = new CreateTokenEffect(
            CardType.CREATURE,
            1,
            "Eldrazi Scion",
            1,
            1,
            null,
            null,
            List.of(CardSubtype.ELDRAZI, CardSubtype.SCION),
            Set.of(),
            Set.of(),
            false,
            false,
            Map.of(),
            List.of(new ActivatedAbility(
                    false,
                    null,
                    List.of(new SacrificeSelfCost(), new AwardManaEffect(ManaColor.COLORLESS)),
                    "Sacrifice this token: Add {C}."
            )),
            false,
            false,
            false,
            0,
            Set.of());

    public DrownerOfHope() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ELDRAZI_SCION.withAmount(2));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasSubtypePredicate(CardSubtype.ELDRAZI),
                                        new PermanentHasSubtypePredicate(CardSubtype.SCION)
                                )),
                                "an Eldrazi Scion"),
                        new TapPermanentsEffect(TapUntapScope.TARGET)
                ),
                "Sacrifice an Eldrazi Scion: Tap target creature.",
                TargetFilters.creature()
        ));
    }
}
