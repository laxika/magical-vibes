package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "247")
public class TrystansCommand extends Card {

    public TrystansCommand() {
        setAllowSharedTargets(true);

        var anyPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player.");
        var elfYouControl = new ControlledPermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.ELF),
                "Target must be an Elf you control.");
        var creatureOrEnchantment = new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsEnchantmentPredicate())),
                "Target must be a creature or enchantment.");

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a token that's a copy of target Elf you control",
                        new CreateTokenCopyOfTargetPermanentEffect(),
                        elfYouControl),
                new ChooseOneEffect.ChooseOneOption(
                        "Return one or two target permanent cards from your graveyard to your hand",
                        new ReturnTargetCardsFromGraveyardToHandEffect(
                                new CardIsPermanentPredicate(), 2)),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target creature or enchantment",
                        new DestroyTargetPermanentEffect(),
                        creatureOrEnchantment),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures target player controls get +3/+3 until end of turn. Untap them",
                        List.of(
                                new BoostAllCreaturesEffect(3, 3, EachPermanentScope.TARGET_PLAYER),
                                new UntapPermanentsEffect(
                                        TapUntapScope.TARGET_PLAYERS_PERMANENTS,
                                        new PermanentIsCreaturePredicate())),
                        anyPlayer)
        ), 2));
    }
}
