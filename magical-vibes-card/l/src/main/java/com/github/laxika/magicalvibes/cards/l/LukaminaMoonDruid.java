package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeLukaminaEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "17")
public class LukaminaMoonDruid extends Card {

    public LukaminaMoonDruid() {
        addBaseAbilities(this);
    }

    public static void addBaseAbilities(Card card) {
        card.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new WasCast(),
                        new SeekLibraryEffect(1, landWithBasicLandType())));

        addSpecializeAbility(card, CardColor.WHITE);
        addSpecializeAbility(card, CardColor.BLUE);
        addSpecializeAbility(card, CardColor.BLACK);
        addSpecializeAbility(card, CardColor.RED);
        addSpecializeAbility(card, CardColor.GREEN);
    }

    private static void addSpecializeAbility(Card card, CardColor color) {
        ActivatedAbility ability = new ActivatedAbility(
                false,
                "{3}",
                List.of(new SpecializeLukaminaEffect(color)),
                "Specialize {3} — " + color.name().toLowerCase(),
                ActivationTimingRestriction.SORCERY_SPEED);
        ability.withActivationCondition(new ControlsPermanentCount(6, new PermanentIsLandPredicate()),
                "Activate only if you control six or more lands.");
        card.addActivatedAbility(ability);
    }

    public static void setBaseFaceCharacteristics(Card card) {
        card.setSetCode("HBG");
        card.setCollectorNumber("17");
        card.setName("Lukamina, Moon Druid");
        card.setManaCost("{2}{G}");
        card.setType(CardType.CREATURE);
        card.setAdditionalTypes(Set.of());
        card.setSupertypes(EnumSet.of(CardSupertype.LEGENDARY));
        card.setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.DRUID));
        card.setColor(CardColor.GREEN);
        card.setColors(List.of(CardColor.GREEN));
        card.setColorIdentity(List.of(CardColor.GREEN));
        card.setKeywords(Set.of());
        card.setPower(2);
        card.setToughness(2);
        card.setCardText("Wild Shape — Specialize {3}. Activate only if you control six or more lands.\n"
                + "When Lukamina, Moon Druid enters, if you cast it, seek a land card with a basic land type.");
    }

    public static void setFaceCharacteristics(Card card, CardColor color) {
        switch (color) {
            case WHITE -> {
                card.setName("Lukamina, Hawk Form");
                card.setManaCost("{2}{G}{W}");
                card.setColors(List.of(CardColor.GREEN, CardColor.WHITE));
                card.setColorIdentity(List.of(CardColor.GREEN, CardColor.WHITE));
                card.setColor(CardColor.WHITE);
                card.setSubtypes(List.of(CardSubtype.BIRD, CardSubtype.DRUID));
                card.setKeywords(Set.of(Keyword.FLYING, Keyword.LIFELINK));
                card.setCardText("Flying, lifelink\n"
                        + "When Lukamina, Hawk Form dies, it unspecializes. If it unspecializes this way, "
                        + "return it to the battlefield tapped.");
            }
            case BLUE -> {
                card.setName("Lukamina, Crocodile Form");
                card.setManaCost("{2}{G}{U}");
                card.setColors(List.of(CardColor.GREEN, CardColor.BLUE));
                card.setColorIdentity(List.of(CardColor.GREEN, CardColor.BLUE));
                card.setColor(CardColor.BLUE);
                card.setSubtypes(List.of(CardSubtype.CROCODILE, CardSubtype.DRUID));
                card.setKeywords(Set.of());
                card.setCardText("When this creature specializes, tap target nonland permanent an opponent controls. "
                        + "That permanent doesn't untap during its controller's untap step for as long as you control "
                        + "Lukamina, Crocodile Form.\n"
                        + "When Lukamina, Crocodile Form dies, it unspecializes. If it unspecializes this way, "
                        + "return it to the battlefield tapped.");
            }
            case BLACK -> {
                card.setName("Lukamina, Scorpion Form");
                card.setManaCost("{2}{B}{G}");
                card.setColors(List.of(CardColor.BLACK, CardColor.GREEN));
                card.setColorIdentity(List.of(CardColor.BLACK, CardColor.GREEN));
                card.setColor(CardColor.BLACK);
                card.setSubtypes(List.of(CardSubtype.SCORPION, CardSubtype.DRUID));
                card.setKeywords(Set.of(Keyword.DEATHTOUCH));
                card.setCardText("Deathtouch\nLukamina, Scorpion Form must be blocked if able.\n"
                        + "When Lukamina, Scorpion Form dies, it unspecializes. If it unspecializes this way, "
                        + "return it to the battlefield tapped.");
            }
            case RED -> {
                card.setName("Lukamina, Wolf Form");
                card.setManaCost("{2}{R}{G}");
                card.setColors(List.of(CardColor.RED, CardColor.GREEN));
                card.setColorIdentity(List.of(CardColor.RED, CardColor.GREEN));
                card.setColor(CardColor.RED);
                card.setSubtypes(List.of(CardSubtype.WOLF, CardSubtype.DRUID));
                card.setKeywords(Set.of(Keyword.MENACE));
                card.setCardText("Menace\nWhenever this creature specializes or attacks, create a 2/2 green Wolf "
                        + "creature token.\nWhen Lukamina, Wolf Form dies, it unspecializes. If it unspecializes "
                        + "this way, return it to the battlefield tapped.");
            }
            case GREEN -> {
                card.setName("Lukamina, Bear Form");
                card.setManaCost("{2}{G}{G}");
                card.setColors(List.of(CardColor.GREEN));
                card.setColorIdentity(List.of(CardColor.GREEN));
                card.setColor(CardColor.GREEN);
                card.setSubtypes(List.of(CardSubtype.BEAR, CardSubtype.DRUID));
                card.setKeywords(Set.of(Keyword.TRAMPLE));
                card.setCardText("Trample\nOther creatures you control get +1/+1 and have trample.\n"
                        + "When Lukamina, Bear Form dies, it unspecializes. If it unspecializes this way, "
                        + "return it to the battlefield tapped.");
            }
            default -> throw new IllegalStateException("Unsupported Lukamina specialization color: " + color);
        }
        card.setPower(4);
        card.setToughness(4);
    }

    private static CardPredicate landWithBasicLandType() {
        return new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.LAND),
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.PLAINS),
                        new CardSubtypePredicate(CardSubtype.ISLAND),
                        new CardSubtypePredicate(CardSubtype.SWAMP),
                        new CardSubtypePredicate(CardSubtype.MOUNTAIN),
                        new CardSubtypePredicate(CardSubtype.FOREST)))));
    }
}
