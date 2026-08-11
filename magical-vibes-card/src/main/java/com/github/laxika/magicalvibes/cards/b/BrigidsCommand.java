package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "208")
public class BrigidsCommand extends Card {

    public BrigidsCommand() {
        setAllowSharedTargets(true);

        var anyPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player.");
        var kithkinYouControl = new ControlledPermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.KITHKIN),
                "Target must be a Kithkin you control.");
        var kithkinToken = new CreateTokenEffect(
                "Kithkin", 1, 1, CardColor.GREEN,
                Set.of(CardColor.GREEN, CardColor.WHITE),
                List.of(CardSubtype.KITHKIN));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a token that's a copy of target Kithkin you control",
                        new CreateTokenCopyOfTargetPermanentEffect(),
                        kithkinYouControl),
                new ChooseOneEffect.ChooseOneOption(
                        "Target player creates a 1/1 green and white Kithkin creature token",
                        new CreateTokenForTargetPlayerEffect(kithkinToken),
                        anyPlayer),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature you control gets +3/+3 until end of turn",
                        new BoostTargetCreatureEffect(3, 3),
                        TargetFilters.creatureYouControl()),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature you control fights target creature an opponent controls",
                        List.of(new FightTargetsEffect()),
                        List.of(TargetFilters.creatureYouControl(), TargetFilters.creatureAnOpponentControls()))
        ), 2));
    }
}
