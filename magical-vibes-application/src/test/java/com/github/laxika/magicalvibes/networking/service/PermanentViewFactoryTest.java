package com.github.laxika.magicalvibes.networking.service;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.GrantedAbilityView;
import com.github.laxika.magicalvibes.networking.model.PermanentView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PermanentViewFactoryTest {

    private final CardViewFactory cardViewFactory = new CardViewFactory();
    private final PermanentViewFactory factory = new PermanentViewFactory(cardViewFactory);

    private Card card(String name, CardType type) {
        Card c = new Card();
        c.setName(name);
        c.setType(type);
        return c;
    }

    /** The plainest projection: no bonuses, no static layer input. */
    private PermanentView create(Permanent p) {
        return factory.create(p, 0, 0, Set.of(), false, List.of());
    }

    private PermanentView createWithExiled(Permanent p, List<Card> faceUpExiled, int faceDownCount) {
        return factory.create(p, 0, 0, Set.of(), false, List.of(), Set.of(), List.of(), Set.of(),
                false, false, false, false, Set.of(), false, Set.of(), List.of(), faceUpExiled, faceDownCount);
    }

    @Test
    @DisplayName("Face-up exiled-with cards are mapped to card views; the face-down count passes through")
    void exiledWithCardsAreMappedToViews() {
        Permanent vat = new Permanent(card("Mimic Vat", CardType.ARTIFACT));
        Card exiled = card("Gravecrawler", CardType.CREATURE);

        PermanentView view = createWithExiled(vat, List.of(exiled), 2);

        assertThat(view.exiledWithCards())
                .extracting(CardView::id, CardView::name)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(exiled.getId(), "Gravecrawler"));
        assertThat(view.faceDownExiledCount()).isEqualTo(2);
        // The factory never reveals face-down cards; the broadcast layer swaps them in per viewer.
        assertThat(view.faceDownExiledCards()).isEmpty();
    }

    @Test
    @DisplayName("Overloads without exiled-with data default to none")
    void overloadsDefaultToNoExiledCards() {
        Permanent vat = new Permanent(card("Mimic Vat", CardType.ARTIFACT));

        PermanentView view = factory.create(vat, 0, 0, Set.of(), false, List.of());

        assertThat(view.exiledWithCards()).isEmpty();
        assertThat(view.faceDownExiledCount()).isZero();
        assertThat(view.faceDownExiledCards()).isEmpty();
    }

    @Test
    @DisplayName("withFaceDownRevealed swaps the card-back count for the cards, preserving everything else")
    void withFaceDownRevealedPreservesOtherFields() {
        Permanent vat = new Permanent(card("Mimic Vat", CardType.ARTIFACT));
        vat.tap();
        Card exiled = card("Gravecrawler", CardType.CREATURE);
        PermanentView view = createWithExiled(vat, List.of(exiled), 1);
        CardView hidden = cardViewFactory.create(card("Hidden Prize", CardType.SORCERY));

        PermanentView revealed = view.withFaceDownRevealed(List.of(hidden));

        assertThat(revealed.faceDownExiledCards()).containsExactly(hidden);
        assertThat(revealed.faceDownExiledCount()).isZero();
        assertThat(revealed.id()).isEqualTo(view.id());
        assertThat(revealed.card()).isEqualTo(view.card());
        assertThat(revealed.tapped()).isTrue();
        assertThat(revealed.exiledWithCards()).isEqualTo(view.exiledWithCards());
    }

    @Test
    @DisplayName("Granted ability views pass through to the permanent view")
    void grantedAbilitiesPassThrough() {
        Permanent creature = new Permanent(card("Voice of All", CardType.CREATURE));
        GrantedAbilityView protection = new GrantedAbilityView(
                "Protection from red", "Voice of All");

        PermanentView view = factory.create(creature, 0, 0, Set.of(), false, List.of())
                .withGrantedAbilities(List.of(protection));

        assertThat(view.grantedAbilities()).containsExactly(protection);
    }

    // ── Keywords ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Granted, until-next-turn and static bonus keywords all land in one granted set")
    void keywordSourcesAreMerged() {
        Permanent creature = new Permanent(card("Grizzly Bears", CardType.CREATURE));
        creature.getGrantedKeywords().add(Keyword.FLYING);
        creature.getUntilNextTurnKeywords().add(Keyword.VIGILANCE);

        PermanentView view = factory.create(creature, 0, 0, Set.of(Keyword.TRAMPLE), false, List.of());

        assertThat(view.grantedKeywords())
                .containsExactlyInAnyOrder(Keyword.FLYING, Keyword.VIGILANCE, Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Removed keywords are reported separately and stripped from the granted set")
    void removedKeywordsAreExcludedFromGranted() {
        Permanent creature = new Permanent(card("Grizzly Bears", CardType.CREATURE));
        creature.getGrantedKeywords().add(Keyword.FLYING);
        creature.getRemovedKeywords().add(Keyword.FLYING);

        PermanentView view = factory.create(creature, 0, 0, Set.of(), false, List.of(), Set.of(), List.of(),
                Set.of(), false, false, false, Set.of(Keyword.FIRST_STRIKE));

        assertThat(view.removedKeywords()).containsExactlyInAnyOrder(Keyword.FLYING, Keyword.FIRST_STRIKE);
        assertThat(view.grantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("A keyword re-granted after its removal (later CR 613.7 timestamp) is not shown as removed")
    void keywordRegrantedAfterRemovalWins() {
        Permanent creature = new Permanent(card("Grizzly Bears", CardType.CREATURE));
        creature.getRemovedKeywords().add(Keyword.FLYING);

        PermanentView view = factory.create(creature, 0, 0, Set.of(Keyword.FLYING), false, List.of());

        assertThat(view.removedKeywords()).doesNotContain(Keyword.FLYING);
        assertThat(view.grantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Losing all abilities removes the card's own keywords, activated abilities and tap ability")
    void losesAllAbilitiesStripsPrintedAbilities() {
        Card printed = card("Llanowar Elves", CardType.CREATURE);
        printed.setKeywords(Set.of(Keyword.FLYING));
        printed.addEffect(EffectSlot.ON_TAP, new DrawCardEffect());
        printed.getActivatedAbilities().add(
                new ActivatedAbility(true, "{0}", List.of(new DrawCardEffect()), "{T}: Draw a card."));
        Permanent creature = new Permanent(printed);

        PermanentView view = factory.create(creature, 0, 0, Set.of(), false, List.of(), Set.of(), List.of(),
                Set.of(), false, false, false, Set.of(), true);

        assertThat(view.removedKeywords()).contains(Keyword.FLYING);
        assertThat(view.card().activatedAbilities()).isEmpty();
        assertThat(view.card().hasTapAbility()).isFalse();
    }

    @Test
    @DisplayName("Summoning sickness clears once haste is granted from any source")
    void hasteClearsSummoningSickness() {
        Permanent creature = new Permanent(card("Grizzly Bears", CardType.CREATURE));

        assertThat(create(creature).summoningSick()).isTrue();
        assertThat(factory.create(creature, 0, 0, Set.of(Keyword.HASTE), false, List.of()).summoningSick()).isFalse();
    }

    // ── Subtypes ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Transient, granted and until-next-turn subtypes merge onto the printed ones without duplicates")
    void grantedSubtypesAreMergedAndDeduplicated() {
        Card printed = card("Adventurer", CardType.CREATURE);
        printed.setSubtypes(List.of(CardSubtype.HUMAN));
        Permanent creature = new Permanent(printed);
        creature.getTransientSubtypes().add(CardSubtype.BARD);
        creature.getGrantedSubtypes().add(CardSubtype.HUMAN);
        creature.getUntilNextTurnSubtypes().add(CardSubtype.WIZARD);

        PermanentView view = create(creature);

        assertThat(view.card().subtypes())
                .containsExactly(CardSubtype.HUMAN, CardSubtype.BARD, CardSubtype.WIZARD);
    }

    @Test
    @DisplayName("Static granted subtypes are additive by default")
    void staticGrantedSubtypesAreAdditive() {
        Card printed = card("Adventurer", CardType.CREATURE);
        printed.setSubtypes(List.of(CardSubtype.HUMAN));
        Permanent creature = new Permanent(printed);

        PermanentView view = factory.create(creature, 0, 0, Set.of(), false, List.of(),
                Set.of(), List.of(CardSubtype.BARD));

        assertThat(view.card().subtypes()).containsExactly(CardSubtype.HUMAN, CardSubtype.BARD);
    }

    @Test
    @DisplayName("Creature-subtype override drops the printed creature types but keeps non-creature ones")
    void subtypeOverrideKeepsNonCreatureSubtypes() {
        Card printed = card("Enchanted Bear", CardType.CREATURE);
        printed.setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.AURA));
        Permanent creature = new Permanent(printed);

        PermanentView view = factory.create(creature, 0, 0, Set.of(), false, List.of(),
                Set.of(), List.of(CardSubtype.BARD), false, true);

        assertThat(view.card().subtypes()).containsExactly(CardSubtype.AURA, CardSubtype.BARD);
    }

    @Test
    @DisplayName("Land-subtype override drops only the basic land types")
    void landSubtypeOverrideDropsBasicLandTypes() {
        Card printed = card("Enchanted Forest", CardType.LAND);
        printed.setSubtypes(List.of(CardSubtype.FOREST, CardSubtype.AURA));
        Permanent land = new Permanent(printed);

        PermanentView view = factory.create(land, 0, 0, Set.of(), false, List.of(),
                Set.of(), List.of(CardSubtype.SWAMP), Set.of(), false, false, true);

        assertThat(view.card().subtypes()).containsExactly(CardSubtype.AURA, CardSubtype.SWAMP);
    }

    @Test
    @DisplayName("An awakening counter turns a non-creature permanent into a green Elemental")
    void awakeningCounterAddsGreenElemental() {
        Card printed = card("Forest", CardType.LAND);
        printed.setSubtypes(List.of(CardSubtype.FOREST));
        Permanent land = new Permanent(printed);
        land.setCounterCount(CounterType.AWAKENING, 1);

        PermanentView view = create(land);

        assertThat(view.card().subtypes()).containsExactly(CardSubtype.FOREST, CardSubtype.ELEMENTAL);
        assertThat(view.card().color()).isEqualTo(CardColor.GREEN);
        assertThat(view.card().colors()).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("An awakening counter on a permanent that is already a creature changes nothing")
    void awakeningCounterSkipsCreatures() {
        Card printed = card("Grizzly Bears", CardType.CREATURE);
        printed.setSubtypes(List.of(CardSubtype.BEAR));
        Permanent creature = new Permanent(printed);
        creature.setCounterCount(CounterType.AWAKENING, 1);

        PermanentView view = create(creature);

        assertThat(view.card().subtypes()).containsExactly(CardSubtype.BEAR);
        assertThat(view.card().color()).isNull();
    }

    // ── Card types and supertypes ─────────────────────────────────────────────

    @Test
    @DisplayName("Card types granted to the permanent are added alongside its printed type")
    void grantedCardTypesAreAdditional() {
        Permanent land = new Permanent(card("Dryad Arbor", CardType.LAND));
        land.getGrantedCardTypes().add(CardType.CREATURE);

        PermanentView view = create(land);

        assertThat(view.card().type()).isEqualTo(CardType.LAND);
        assertThat(view.card().additionalTypes()).contains(CardType.CREATURE);
    }

    @Test
    @DisplayName("A permanently animated permanent becomes an additional creature type and reads as animated")
    void permanentAnimationAddsCreatureType() {
        Permanent land = new Permanent(card("Inkmoth Nexus", CardType.LAND));
        land.setPermanentlyAnimated(true);

        PermanentView view = create(land);

        assertThat(view.card().additionalTypes()).contains(CardType.CREATURE);
        assertThat(view.animatedCreature()).isTrue();
    }

    @Test
    @DisplayName("A card-type override replaces the printed type, demoting the rest to additional types")
    void cardTypeOverrideReplacesPrintedType() {
        Permanent land = new Permanent(card("Mishra's Factory", CardType.LAND));

        PermanentView view = factory.create(land, 0, 0, Set.of(), false, List.of(), Set.of(), List.of(),
                EnumSet.of(CardType.CREATURE, CardType.ARTIFACT), false, false, false, true, Set.of(), false,
                Set.of(), List.of(), List.of(), 0);

        assertThat(view.card().type()).isEqualTo(CardType.CREATURE);
        assertThat(view.card().additionalTypes()).containsExactly(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("A card-type override with no granted types falls back to Land")
    void emptyCardTypeOverrideFallsBackToLand() {
        Permanent creature = new Permanent(card("Grizzly Bears", CardType.CREATURE));

        PermanentView view = factory.create(creature, 0, 0, Set.of(), false, List.of(), Set.of(), List.of(),
                Set.of(), false, false, false, true, Set.of(), false, Set.of(), List.of(), List.of(), 0);

        assertThat(view.card().type()).isEqualTo(CardType.LAND);
        assertThat(view.card().additionalTypes()).isEmpty();
    }

    @Test
    @DisplayName("Static granted supertypes merge with the printed ones")
    void staticGrantedSupertypesAreMerged() {
        Card printed = card("Snow Bear", CardType.CREATURE);
        printed.setSupertypes(Set.of(CardSupertype.SNOW));
        Permanent creature = new Permanent(printed);

        PermanentView view = factory.create(creature, 0, 0, Set.of(), false, List.of(), Set.of(), List.of(),
                Set.of(), false, false, false, Set.of(), false, Set.of(CardSupertype.LEGENDARY));

        assertThat(view.card().supertypes())
                .containsExactlyInAnyOrder(CardSupertype.SNOW, CardSupertype.LEGENDARY);
    }

    // ── Colors ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Colors granted to the permanent are added to its printed colors")
    void grantedColorsAreAdditive() {
        Card printed = card("Bloodbraid Elf", CardType.CREATURE);
        printed.setColor(CardColor.RED);
        printed.setColors(List.of(CardColor.RED));
        Permanent creature = new Permanent(printed);
        creature.getGrantedColors().add(CardColor.BLACK);

        PermanentView view = create(creature);

        assertThat(view.card().color()).isEqualTo(CardColor.RED);
        assertThat(view.card().colors()).containsExactlyInAnyOrder(CardColor.RED, CardColor.BLACK);
    }

    @Test
    @DisplayName("A transient color override replaces the printed colors outright")
    void transientColorOverrideReplacesColors() {
        Card printed = card("Bloodbraid Elf", CardType.CREATURE);
        printed.setColor(CardColor.RED);
        printed.setColors(List.of(CardColor.RED));
        Permanent creature = new Permanent(printed);
        creature.setColorOverridden(true);
        creature.getTransientColors().add(CardColor.BLUE);

        PermanentView view = create(creature);

        assertThat(view.card().color()).isEqualTo(CardColor.BLUE);
        assertThat(view.card().colors()).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("A static color override with an empty set makes the permanent colorless")
    void emptyColorOverrideMakesColorless() {
        Card printed = card("Bloodbraid Elf", CardType.CREATURE);
        printed.setColor(CardColor.RED);
        printed.setColors(List.of(CardColor.RED));
        Permanent creature = new Permanent(printed);

        PermanentView view = factory.create(creature, 0, 0, Set.of(), false, List.of(),
                Set.of(), List.of(), true, false);

        assertThat(view.card().color()).isNull();
        assertThat(view.card().colors()).isEmpty();
    }

    // ── Text, counters and power/toughness ────────────────────────────────────

    @Test
    @DisplayName("Text replacements are applied to the projected rules text")
    void textReplacementsRewriteCardText() {
        Card printed = card("Mountaineer", CardType.CREATURE);
        printed.setCardText("Tap: Add one mana for each Mountain you control.");
        Permanent creature = new Permanent(printed);
        creature.getTextReplacements().add(new TextReplacement("Mountain", "Island"));

        PermanentView view = create(creature);

        assertThat(view.card().cardText()).isEqualTo("Tap: Add one mana for each Island you control.");
    }

    @Test
    @DisplayName("Counters are copied onto the view and folded into effective power/toughness")
    void countersAndBonusesReachTheView() {
        Card printed = card("Grizzly Bears", CardType.CREATURE);
        printed.setPower(2);
        printed.setToughness(2);
        Permanent creature = new Permanent(printed);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        creature.setPowerModifier(1);

        PermanentView view = factory.create(creature, 3, 0, Set.of(), false, List.of());

        assertThat(view.counters()).containsEntry(CounterType.PLUS_ONE_PLUS_ONE, 2);
        assertThat(view.powerModifier()).isEqualTo(4);
        assertThat(view.effectivePower()).isEqualTo(8);
        assertThat(view.effectiveToughness()).isEqualTo(4);
    }
}
