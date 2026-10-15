package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureRandomCreatureFromOpponentLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityToCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostOtherControlledCreaturesAndHandCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTiming;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeLaezelEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringSpellOpponentControllerConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "2")
public class LaezelGithyankiWarrior extends Card {

    public LaezelGithyankiWarrior() {
        addEffect(EffectSlot.ON_SELF_CAST,
                new GrantTriggeredAbilityToCastSpellEffect(
                        EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY,
                        new TriggeringSpellOpponentControllerConditionalEffect(
                                new MayEffect(
                                        new FlickerEffect(FlickerScope.SELF, null, ReturnTiming.IMMEDIATE,
                                                TurnStep.END_STEP, false, null, null, 0, false, false),
                                        "Exile this creature, then return it to the battlefield under its owner's control?"))));

        addSpecializeAbility(CardColor.WHITE, CardSubtype.PLAINS);
        addSpecializeAbility(CardColor.BLUE, CardSubtype.ISLAND);
        addSpecializeAbility(CardColor.BLACK, CardSubtype.SWAMP);
        addSpecializeAbility(CardColor.RED, CardSubtype.MOUNTAIN);
        addSpecializeAbility(CardColor.GREEN, CardSubtype.FOREST);
    }

    private void addSpecializeAbility(CardColor color, CardSubtype basicLandType) {
        CardPredicate discardFilter = new CardAnyOfPredicate(List.of(
                new CardColorPredicate(color), new CardSubtypePredicate(basicLandType)));
        List<CardEffect> effects = new ArrayList<>();
        effects.add(new DiscardCardTypeCost(discardFilter,
                color.name().toLowerCase() + " card or " + basicLandType.name().toLowerCase()));
        effects.add(new SpecializeLaezelEffect(color));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                effects,
                "Specialize {1} — " + color.name().toLowerCase()
                        + " (discard a " + color.name().toLowerCase() + " card or "
                        + basicLandType.name().toLowerCase() + ")",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    public static CardEffect specializedTrigger(CardColor color) {
        return switch (color) {
            case WHITE -> new SeekLibraryEffect(
                    new Fixed(1),
                    new CardAllOfPredicate(List.of(
                            new CardIsPermanentPredicate(),
                            new CardNotPredicate(new CardTypePredicate(CardType.LAND)))),
                    LibrarySearchDestination.HAND,
                    new ManaValueBound(new Fixed(3), false, 0));
            case BLUE -> new ConjureRandomCreatureFromOpponentLibraryEffect();
            case BLACK -> new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                    new CardTypePredicate(CardType.CREATURE), 2, 3);
            case RED -> CreateTokenEffect.whiteSoldier(2);
            case GREEN -> new PerpetuallyBoostOtherControlledCreaturesAndHandCardsEffect(1, 1);
        };
    }
}
