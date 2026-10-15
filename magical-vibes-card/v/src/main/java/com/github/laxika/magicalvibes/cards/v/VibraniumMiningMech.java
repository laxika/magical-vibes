package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "117")
@CardRegistration(set = "MSC", collectorNumber = "458")
public class VibraniumMiningMech extends Card {

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

    public VibraniumMiningMech() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, VIBRANIUM_TOKEN);
        addEffect(EffectSlot.ON_ATTACK, VIBRANIUM_TOKEN);

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new BoostSelfEffect(1, 0)),
                "{2}: This Vehicle gets +1/+0 until end of turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(2), AnimatePermanentsEffect.crew()),
                "Crew 2"
        ));
    }
}
