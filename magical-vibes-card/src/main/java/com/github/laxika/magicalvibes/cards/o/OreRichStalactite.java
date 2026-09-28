package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.CosmiumCatalyst;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.CraftMaterialCost;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceFromExileTransformedEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCC", collectorNumber = "11")
@CardRegistration(set = "LCC", collectorNumber = "23")
public class OreRichStalactite extends Card {

    public OreRichStalactite() {
        setBackFaceCard(new CosmiumCatalyst());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(
                        ManaColor.RED,
                        1,
                        new ManaRestriction.SpellTypes(Set.of(CardType.INSTANT, CardType.SORCERY))
                )),
                "{T}: Add {R}. Spend this mana only to cast an instant or sorcery spell."
        ));

        CardAllOfPredicate redInstantOrSorcery = new CardAllOfPredicate(List.of(
                new CardColorPredicate(CardColor.RED),
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)
                ))
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}{R}",
                List.of(
                        new ExileSelfCost(),
                        CraftMaterialCost.fromGraveyard(4, redInstantOrSorcery),
                        new ReturnSourceFromExileTransformedEffect()
                ),
                "Craft with four or more red instant and/or sorcery cards {3}{R}{R} ({3}{R}{R}, "
                        + "Exile this artifact, Exile the four or more from your graveyard: Return this card "
                        + "transformed under its owner's control. Craft only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    @java.lang.Override
    public String getBackFaceClassName() {
        return "CosmiumCatalyst";
    }
}
