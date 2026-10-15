package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "70")
public class UrzasCommand extends Card {

    public UrzasCommand() {
        PermanentCount artifactsYouControl =
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER);
        CreateTokenEffect constructToken = new CreateTokenEffect(
                CardType.CREATURE, 1, "Construct", 0, 0, null, null,
                List.of(CardSubtype.CONSTRUCT), Set.of(), Set.of(CardType.ARTIFACT), false, true,
                Map.of(EffectSlot.STATIC, new BoostSelfEffect(artifactsYouControl, artifactsYouControl)), List.of(),
                false, false, false, 0, Set.of());

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you don't control get -2/-0 until end of turn",
                        new BoostAllCreaturesEffect(-2, 0,
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                new ChooseOneEffect.ChooseOneOption(
                        "Create a tapped Powerstone token",
                        CreateTokenEffect.ofPowerstoneToken(new Fixed(1))),
                new ChooseOneEffect.ChooseOneOption(
                        "Create a tapped 0/0 Construct artifact creature token",
                        constructToken),
                new ChooseOneEffect.ChooseOneOption(
                        "Scry 1, then draw a card",
                        List.of(new ScryEffect(1), new DrawCardEffect()))
        ), 2));
    }
}
