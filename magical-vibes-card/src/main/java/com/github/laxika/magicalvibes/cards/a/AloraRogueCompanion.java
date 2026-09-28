package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentCreatureAndPerpetuallyBoostEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedEndStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureToHandAndPerpetuallyBoostEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandThenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeAloraEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.ArrayList;
import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "5")
public class AloraRogueCompanion extends Card {

    public AloraRogueCompanion() {
        addAttackAbility(this, ReturnToHandEffect.target());

        addSpecializeAbility(CardColor.WHITE, CardSubtype.PLAINS);
        addSpecializeAbility(CardColor.BLUE, CardSubtype.ISLAND);
        addSpecializeAbility(CardColor.BLACK, CardSubtype.SWAMP);
        addSpecializeAbility(CardColor.RED, CardSubtype.MOUNTAIN);
        addSpecializeAbility(CardColor.GREEN, CardSubtype.FOREST);
    }

    public static void addAttackAbility(Card card, CardEffect delayedReturnEffect) {
        card.target(TargetFilters.attackingCreature(), 0, 1)
                .addEffect(EffectSlot.ON_ATTACK, new MakeCreatureUnblockableEffect())
                .addEffect(EffectSlot.ON_ATTACK,
                        new RegisterDelayedEndStepTriggerEffect(List.of(), delayedReturnEffect));
    }

    private void addSpecializeAbility(CardColor color, CardSubtype basicLandType) {
        CardPredicate discardFilter = new CardAnyOfPredicate(List.of(
                new CardColorPredicate(color), new CardSubtypePredicate(basicLandType)));
        List<CardEffect> effects = new ArrayList<>();
        effects.add(new DiscardCardTypeCost(discardFilter,
                color.name().toLowerCase() + " card or " + basicLandType.name().toLowerCase()));
        effects.add(new SpecializeAloraEffect(color));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                effects,
                "Specialize {2} — " + color.name().toLowerCase()
                        + " (discard a " + color.name().toLowerCase() + " card or "
                        + basicLandType.name().toLowerCase() + ")",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    public static CardEffect specializedReturnEffect(CardColor color) {
        return switch (color) {
            case WHITE -> new ReturnTargetPermanentToHandThenEffect(
                    CreateTokenEffect.whiteSoldier(1), ThenEffectRecipient.CONTROLLER);
            case BLUE -> new ReturnTargetPermanentToHandThenEffect(
                    new ChooseOpponentCreatureAndPerpetuallyBoostEffect(-1, 0),
                    ThenEffectRecipient.CONTROLLER);
            case BLACK -> new ReturnTargetPermanentToHandThenEffect(
                    new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT),
                    ThenEffectRecipient.CONTROLLER);
            case RED -> new ReturnTargetPermanentToHandThenEffect(
                    CreateTokenEffect.ofTreasureToken(1), ThenEffectRecipient.CONTROLLER);
            case GREEN -> new ReturnTargetCreatureToHandAndPerpetuallyBoostEffect(1, 1);
        };
    }

    public static String specializedCardText(CardColor color) {
        String common = "Whenever you attack, up to one target attacking creature can't be blocked this turn. "
                + "At the beginning of the next end step, return that creature to its owner's hand.";
        return switch (color) {
            case WHITE -> common + " If you do, create a 1/1 white Soldier creature token.";
            case BLUE -> common + " If you do, a creature of your choice an opponent controls perpetually gets -1/-0.";
            case BLACK -> common + " If you do, each opponent loses 2 life.";
            case RED -> common + " If you do, create a Treasure token.";
            case GREEN -> common + " If you do, it perpetually gets +1/+1.";
        };
    }
}
