package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantStaticEffectToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.SpellTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "6")
public class GaleConduitOfTheArcane extends Card {

    public GaleConduitOfTheArcane() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new WasCast()),
                ReturnTargetCardsFromGraveyardToHandEffect.exactlyOneForTriggeredAbility(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY)))));

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
        effects.add(new SpecializeEffect(color, GaleConduitOfTheArcane::specializedFace));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                effects,
                "Specialize {2} — " + color.name().toLowerCase()
                        + " (discard a " + color.name().toLowerCase() + " card or "
                        + basicLandType.name().toLowerCase() + ")",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    /**
     * Gives {@code card}, Gale specialized into {@code color}, its "whenever you cast an instant or sorcery spell"
     * ability.
     */
    public static void addSpecializedSpellCastTrigger(Card card, CardColor color) {
        SpellTrigger instantOrSorcery = Triggers.youCastSpell().withFilter(new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT), new CardTypePredicate(CardType.SORCERY))));
        switch (color) {
            case WHITE -> card.addTrigger(instantOrSorcery,
                    new CreateTokenEffect("Pegasus", 1, 1, CardColor.WHITE,
                            List.of(CardSubtype.PEGASUS), Set.of(Keyword.FLYING), Set.of()));
            case BLUE -> card.addTrigger(instantOrSorcery,
                    new DrawCardEffect(), new DiscardEffect(1, Subjects.you()));
            case BLACK -> card.addTrigger(instantOrSorcery,
                    new LoseLifeEffect(2, Subjects.eachOpponent()));
            case RED -> card.addTrigger(instantOrSorcery,
                    new PerpetuallyGrantStaticEffectToSourceEffect(
                            new StaticBoostEffect(1, 0, Subjects.creaturesYouControl())));
            case GREEN -> card.target(TargetFilters.creature()).addTrigger(instantOrSorcery,
                    new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2));
            default -> throw new IllegalArgumentException("Gale doesn't specialize into " + color);
        }
    }

    public static void setSpecializedBaseCharacteristics(Card card) {
        card.setSetCode("HBG");
        card.setCollectorNumber("6");
        card.setType(CardType.CREATURE);
        card.setAdditionalTypes(Set.of());
        card.setSupertypes(EnumSet.of(CardSupertype.LEGENDARY));
        card.setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.WIZARD));
        card.setColor(CardColor.BLUE);
        card.setColors(List.of(CardColor.BLUE));
        card.setColorIdentity(List.of(CardColor.BLUE));
        card.setKeywords(Set.of());
    }

    /** Writes Gale's specialized face for {@code color}; it has no "when this creature specializes" ability. */
    public static List<CardEffect> specializedFace(Card face, CardColor color) {
        setSpecializedBaseCharacteristics(face);
        face.setPower(4);
        face.setToughness(5);

        switch (color) {
            case WHITE -> {
                face.setName("Gale, Holy Conduit");
                face.setManaCost("{3}{W}{U}");
                face.setColors(List.of(CardColor.WHITE, CardColor.BLUE));
                face.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLUE));
                face.setColor(CardColor.WHITE);
                face.setCardText("Whenever you cast an instant or sorcery spell, create a 1/1 white Pegasus creature token with flying.");
            }
            case BLUE -> {
                face.setName("Gale, Temporal Conduit");
                face.setManaCost("{3}{U}{U}");
                face.setCardText("Whenever you cast an instant or sorcery spell, draw a card, then discard a card.");
            }
            case BLACK -> {
                face.setName("Gale, Abyssal Conduit");
                face.setManaCost("{3}{U}{B}");
                face.setColors(List.of(CardColor.BLUE, CardColor.BLACK));
                face.setColorIdentity(List.of(CardColor.BLUE, CardColor.BLACK));
                face.setColor(CardColor.BLACK);
                face.setCardText("Whenever you cast an instant or sorcery spell, each opponent loses 2 life.");
            }
            case RED -> {
                face.setName("Gale, Storm Conduit");
                face.setManaCost("{3}{U}{R}");
                face.setColors(List.of(CardColor.BLUE, CardColor.RED));
                face.setColorIdentity(List.of(CardColor.BLUE, CardColor.RED));
                face.setColor(CardColor.RED);
                face.setCardText("Whenever you cast an instant or sorcery spell, Gale, Storm Conduit perpetually gains \"Creatures you control get +1/+0.\"");
            }
            case GREEN -> {
                face.setName("Gale, Primeval Conduit");
                face.setManaCost("{3}{G}{U}");
                face.setColors(List.of(CardColor.GREEN, CardColor.BLUE));
                face.setColorIdentity(List.of(CardColor.GREEN, CardColor.BLUE));
                face.setColor(CardColor.GREEN);
                face.setCardText("Whenever you cast an instant or sorcery spell, put two +1/+1 counters on target creature.");
            }
            default -> throw new IllegalStateException("Unsupported Gale specialization color: "
                    + color);
        }

        addSpecializedSpellCastTrigger(face, color);
        return List.of();
    }
}
