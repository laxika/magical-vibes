package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayTapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "39")
public class ThousandMoonsSmithy extends Card {

    public ThousandMoonsSmithy() {
        setBackFaceCard(new BarracksOfTheThousand());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, gnomeToken());

        PermanentAnyOfPredicate artifactsOrCreatures = artifactsOrCreatures();
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new MayPayTapPermanentsEffect(
                        new TapMultiplePermanentsCost(5, artifactsOrCreatures),
                        new TransformSelfEffect(),
                        "Tap five untapped artifacts and/or creatures to transform Thousand Moons Smithy?"));
    }

    static CreateTokenEffect gnomeToken() {
        PermanentCount artifactsOrCreatures = new PermanentCount(artifactsOrCreatures(), CountScope.CONTROLLER);
        return new CreateTokenEffect(
                1,
                "Gnome Soldier",
                0,
                0,
                CardColor.WHITE,
                List.of(CardSubtype.GNOME, CardSubtype.SOLDIER),
                Set.of(),
                Set.of(CardType.ARTIFACT),
                Map.of(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(artifactsOrCreatures, artifactsOrCreatures)));
    }

    private static PermanentAnyOfPredicate artifactsOrCreatures() {
        return new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()));
    }

    @Override
    public String getBackFaceClassName() {
        return "BarracksOfTheThousand";
    }
}
