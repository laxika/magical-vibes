package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EntwineManaCost;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "ONC", collectorNumber = "22")
@CardRegistration(set = "ONC", collectorNumber = "32")
public class MirageMockery extends Card {

    public MirageMockery() {
        var artifactCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()));
        var nonartifactCreature = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsArtifactPredicate()),
                new PermanentIsCreaturePredicate()));

        addEffect(EffectSlot.SPELL, new EntwineManaCost("{2}{U}"));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a token that's a copy of target artifact creature you control",
                        new CreateTokenCopyOfTargetPermanentEffect(),
                        new ControlledPermanentPredicateTargetFilter(
                                artifactCreature, "Target must be an artifact creature you control.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Create a token that's a copy of target nonartifact creature you control",
                        new CreateTokenCopyOfTargetPermanentEffect(),
                        new ControlledPermanentPredicateTargetFilter(
                                nonartifactCreature, "Target must be a nonartifact creature you control."))
        ), false, 1, 2, false));
    }
}
