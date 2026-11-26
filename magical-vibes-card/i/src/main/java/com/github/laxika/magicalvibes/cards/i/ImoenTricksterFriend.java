package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfAttackingAloneEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PlayAdditionalLandsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "7")
public class ImoenTricksterFriend extends Card {

    private static final CardPredicate INSTANT_OR_SORCERY = new CardAnyOfPredicate(List.of(
            new CardTypePredicate(CardType.INSTANT), new CardTypePredicate(CardType.SORCERY)));

    public ImoenTricksterFriend() {
        addStaticEffect(new CantBeBlockedIfAttackingAloneEffect());

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
        effects.add(new ReduceActivationCostEffect(new FixedIfCondition(
                new GraveyardCardThreshold(2, INSTANT_OR_SORCERY), 3, 0)));
        effects.add(new DiscardCardTypeCost(discardFilter,
                color.name().toLowerCase() + " card or " + basicLandType.name().toLowerCase()));
        effects.add(new SpecializeEffect(color, ImoenTricksterFriend::specializedFace));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                effects,
                "Specialize {5} — " + color.name().toLowerCase()
                        + " (discard a " + color.name().toLowerCase() + " card or "
                        + basicLandType.name().toLowerCase()
                        + "). This ability costs {3} less to activate if there are two or more "
                        + "instant and/or sorcery cards in your graveyard.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    public static CardEffect specializedCombatDamageTrigger(CardColor color) {
        CardEffect effect = switch (color) {
            case WHITE -> new PutCounterOnEachControlledPermanentEffect(
                    CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate());
            case BLUE -> {
                PermanentPredicate opponentCreature = TargetFilters.creatureAnOpponentControls().predicate();
                yield new QueueReflexiveAbilityEffect(SequenceEffect.of(
                        new TapPermanentsEffect(Subjects.target(), opponentCreature),
                        new SkipNextUntapEffect(Subjects.target())));
            }
            case BLACK -> CreateTokenEffect.blackZombie(1);
            case RED -> DealDamageEffect.to(Subjects.eachOpponent(), 2);
            case GREEN -> SequenceEffect.of(new DrawCardEffect(), new PlayAdditionalLandsEffect(1));
        };
        // The specialized face's trigger wraps this in its "you may" (specializedFace).
        return SequenceEffect.of(
                ExileGraveyardCardsEffect.fromOwnGraveyard(1, INSTANT_OR_SORCERY),
                new ConditionalEffect(HappenedThisWay.exiled(), effect));
    }

    public static void setSpecializedBaseCharacteristics(Card card) {
        card.setSetCode("HBG");
        card.setCollectorNumber("7");
        card.setType(CardType.CREATURE);
        card.setAdditionalTypes(Set.of());
        card.setSupertypes(EnumSet.of(CardSupertype.LEGENDARY));
        card.setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.ROGUE, CardSubtype.WIZARD));
        card.setColor(CardColor.BLUE);
        card.setColors(List.of(CardColor.BLUE));
        card.setColorIdentity(List.of(CardColor.BLUE));
        card.setKeywords(Set.of());
    }

    /** Writes Imoen's specialized face for {@code color}; it has no "when this creature specializes" ability. */
    public static List<CardEffect> specializedFace(Card face, CardColor color) {
        setSpecializedBaseCharacteristics(face);
        face.setPower(3);
        face.setToughness(2);
        face.addStaticEffect(new CantBeBlockedEffect());

        switch (color) {
            case WHITE -> {
                face.setName("Imoen, Honorable Trickster");
                face.setManaCost("{1}{W}{U}");
                face.setColors(List.of(CardColor.WHITE, CardColor.BLUE));
                face.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLUE));
                face.setColor(CardColor.WHITE);
                face.setCardText("Imoen can't be blocked. Whenever Imoen deals combat damage to a player, you may exile an instant or sorcery card from your graveyard. If you do, put a +1/+1 counter on each creature you control.");
            }
            case BLUE -> {
                face.setName("Imoen, Wily Trickster");
                face.setManaCost("{1}{U}{U}");
                face.setCardText("Imoen can't be blocked. Whenever Imoen deals combat damage to a player, you may exile an instant or sorcery card from your graveyard. If you do, tap target creature an opponent controls. That creature doesn't untap during its controller's next untap step.");
            }
            case BLACK -> {
                face.setName("Imoen, Occult Trickster");
                face.setManaCost("{1}{U}{B}");
                face.setColors(List.of(CardColor.BLUE, CardColor.BLACK));
                face.setColorIdentity(List.of(CardColor.BLUE, CardColor.BLACK));
                face.setColor(CardColor.BLACK);
                face.setCardText("Imoen can't be blocked. Whenever Imoen deals combat damage to a player, you may exile an instant or sorcery card from your graveyard. If you do, create a 2/2 black Zombie creature token.");
            }
            case RED -> {
                face.setName("Imoen, Chaotic Trickster");
                face.setManaCost("{1}{U}{R}");
                face.setColors(List.of(CardColor.BLUE, CardColor.RED));
                face.setColorIdentity(List.of(CardColor.BLUE, CardColor.RED));
                face.setColor(CardColor.RED);
                face.setCardText("Imoen can't be blocked. Whenever Imoen deals combat damage to a player, you may exile an instant or sorcery card from your graveyard. If you do, Imoen deals 2 damage to each opponent.");
            }
            case GREEN -> {
                face.setName("Imoen, Wise Trickster");
                face.setManaCost("{1}{U}{G}");
                face.setColors(List.of(CardColor.BLUE, CardColor.GREEN));
                face.setColorIdentity(List.of(CardColor.BLUE, CardColor.GREEN));
                face.setColor(CardColor.GREEN);
                face.setCardText("Imoen can't be blocked. Whenever Imoen deals combat damage to a player, you may exile an instant or sorcery card from your graveyard. If you do, draw a card and you may play an additional land this turn.");
            }
            default -> throw new IllegalStateException("Unsupported Imoen specialization color: "
                    + color);
        }

        face.addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayEffect(specializedCombatDamageTrigger(color),
                        "You may exile an instant or sorcery card from your graveyard."));
        return List.of();
    }
}
