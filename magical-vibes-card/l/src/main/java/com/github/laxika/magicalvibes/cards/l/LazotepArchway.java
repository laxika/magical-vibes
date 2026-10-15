package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "376")
@CardRegistration(set = "MB2", collectorNumber = "613")
public class LazotepArchway extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("LazotepArchway", new OracleData(
                "Lazotep Archway",
                CardType.LAND,
                Set.of(),
                null,
                null,
                List.of(),
                List.of(CardColor.WHITE, CardColor.BLACK),
                Set.of(),
                List.of(),
                "Lazotep Archway enters the battlefield tapped.\n"
                        + "{T}: Add {W} or {B}.\n"
                        + "Eternalize {3}{W}{B} ({3}{W}{B}, Exile this card from your graveyard: Create a token "
                        + "that's a copy of it, except it's a 4/4 black Zombie creature and loses all other card "
                        + "types. Eternalize only as a sorcery. And don't forget it still enters tapped!)",
                null,
                null,
                Set.of(),
                null,
                null,
                null));
    }

    public LazotepArchway() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.WHITE, ManaColor.BLACK))),
                "{T}: Add {W} or {B}."
        ));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}{B}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new CreateTokenCopyOfSourceEffect(
                                false,
                                new Fixed(1),
                                CardColor.BLACK,
                                CardSubtype.ZOMBIE,
                                true,
                                4,
                                4,
                                false,
                                false,
                                Map.of(),
                                false,
                                Set.of(CardType.CREATURE)
                        )
                ),
                "Eternalize {3}{W}{B} ({3}{W}{B}, Exile this card from your graveyard: Create a token "
                        + "that's a copy of it, except it's a 4/4 black Zombie creature and loses all other card "
                        + "types. Eternalize only as a sorcery. And don't forget it still enters tapped!)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
