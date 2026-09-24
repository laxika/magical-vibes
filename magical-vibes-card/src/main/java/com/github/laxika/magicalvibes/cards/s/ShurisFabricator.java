package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "114")
@CardRegistration(set = "MSC", collectorNumber = "450")
public class ShurisFabricator extends Card {

    private static final CreateTokenEffect VIBRANIUM = new CreateTokenEffect(
            CardType.ARTIFACT,
            2,
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

    public ShurisFabricator() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, VIBRANIUM);
        addActivatedAbility(new ActivatedAbility(
                true,
                "{6}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.ARTIFACT))
                        .targetGraveyard(true)
                        .enterWithCounter(CounterType.FINALITY)
                        .enterWithCounterCount(1)
                        .build()),
                "{6}, {T}: Return target artifact card from your graveyard to the battlefield with a finality counter on it. "
                        + "Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
