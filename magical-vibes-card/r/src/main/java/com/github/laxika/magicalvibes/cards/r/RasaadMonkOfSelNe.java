package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallySetTargetExiledCreatureBasePowerToughnessAndLoseAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeRasaadEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "4")
public class RasaadMonkOfSelNe extends Card {

    public RasaadMonkOfSelNe() {
        target(TargetFilters.creatureAnOpponentControls()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTargetPermanentAndTrackWithSourceEffect());
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new ReturnAllCardsExiledWithSourceEffect());

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
        effects.add(new SpecializeRasaadEffect(color));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                effects,
                "Specialize {5} — " + color.name().toLowerCase()
                        + " (discard a " + color.name().toLowerCase() + " card or "
                        + basicLandType.name().toLowerCase() + ")",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    public static CardEffect specializedTrigger(CardColor color) {
        return switch (color) {
            case WHITE -> new PerpetuallySetTargetExiledCreatureBasePowerToughnessAndLoseAbilitiesEffect();
            case BLUE -> new CreateTokenEffect(2, "Faerie Dragon", 1, 1, CardColor.BLUE,
                    List.of(CardSubtype.FAERIE, CardSubtype.DRAGON), Set.of(Keyword.FLYING), Set.of());
            case BLACK -> new CreateTokenEffect("Skeleton", 4, 1, CardColor.BLACK,
                    List.of(CardSubtype.SKELETON), Set.of(Keyword.MENACE), Set.of());
            case RED -> CreateTokenEffect.whiteSoldier(3);
            case GREEN -> new CreateTokenEffect(2, "Boar", 2, 2, CardColor.GREEN,
                    List.of(CardSubtype.BOAR), Set.of(), Set.of());
        };
    }
}
