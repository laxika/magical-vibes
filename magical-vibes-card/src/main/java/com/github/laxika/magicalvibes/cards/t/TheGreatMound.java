package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "120")
@CardRegistration(set = "MSC", collectorNumber = "479")
public class TheGreatMound extends Card {

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

    public TheGreatMound() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(VIBRANIUM_TOKEN),
                "{3}, {T}: Create a tapped Vibranium token."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{6}",
                List.of(new DrawCardEffect(1)),
                "{6}, {T}: Draw a card."
        ));
    }
}
