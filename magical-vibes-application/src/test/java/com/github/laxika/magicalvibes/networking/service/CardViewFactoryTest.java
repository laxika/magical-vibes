package com.github.laxika.magicalvibes.networking.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.ModalOptionView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CardViewFactoryTest {

    private final CardViewFactory factory = new CardViewFactory();

    private Card card(String name, CardType type) {
        Card c = new Card();
        c.setName(name);
        c.setType(type);
        return c;
    }

    /** A prepare card: the Prepared keyword plus a back face holding the prepare spell. */
    private Card prepareCard() {
        Card spell = card("Heroic Stanza", CardType.SORCERY);
        spell.setManaCost("{1}{W/B}");
        spell.setCardText("Put a +1/+1 counter on target creature.");

        Card front = card("Abigale, Poet Laureate", CardType.CREATURE);
        front.setKeywords(Set.of(Keyword.FLYING, Keyword.PREPARED));
        front.setBackFaceCard(spell);
        return front;
    }

    @Test
    @DisplayName("A prepare card projects its prepare spell as a nested view")
    void prepareCardProjectsItsPrepareSpell() {
        CardView view = factory.create(prepareCard());

        assertThat(view.prepareSpell()).isNotNull();
        assertThat(view.prepareSpell().name()).isEqualTo("Heroic Stanza");
        assertThat(view.prepareSpell().manaCost()).isEqualTo("{1}{W/B}");
        assertThat(view.prepareSpell().type()).isEqualTo(CardType.SORCERY);
        assertThat(view.prepareSpell().cardText()).isEqualTo("Put a +1/+1 counter on target creature.");
        // The prepare spell has no prepare spell of its own, so the nesting terminates.
        assertThat(view.prepareSpell().prepareSpell()).isNull();
    }

    @Test
    @DisplayName("A transform card's back face is not projected as a prepare spell")
    void transformBackFaceIsNotAPrepareSpell() {
        Card front = card("Huntmaster of the Fells", CardType.CREATURE);
        front.setKeywords(Set.of(Keyword.TRANSFORM));
        front.setBackFaceCard(card("Ravager of the Fells", CardType.CREATURE));

        assertThat(factory.create(front).prepareSpell()).isNull();
    }

    @Test
    @DisplayName("Only a split card's fused mode is marked as castable from hand alone")
    void splitCardFusedModeIsHandOnly() {
        Card card = card("Wax // Wane", CardType.INSTANT);
        card.setKeywords(Set.of(Keyword.FUSE));
        card.setSplitHalves(List.of(splitHalf("Wax", "{G}"), splitHalf("Wane", "{W}")));
        card.addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Wax", List.of(new DrawCardEffect(1))).withManaCost("{G}"),
                new ChooseOneEffect.ChooseOneOption("Wane", List.of(new DrawCardEffect(1))).withManaCost("{W}"),
                new ChooseOneEffect.ChooseOneOption("Fuse — Wax and then Wane",
                        List.of(new DrawCardEffect(1), new DrawCardEffect(1))).withManaCost("{G}{W}"))));

        assertThat(factory.create(card).modalOptions())
                .extracting(ModalOptionView::handOnly)
                .containsExactly(false, false, true);
    }

    private static OracleData splitHalf(String name, String manaCost) {
        return new OracleData(name, CardType.INSTANT, Set.of(), manaCost, null, List.of(), List.of(),
                Set.of(), List.of(), "", null, null, Set.of(), null, null, null);
    }

    @Test
    @DisplayName("A plain card has no prepare spell")
    void plainCardHasNoPrepareSpell() {
        assertThat(factory.create(card("Grizzly Bears", CardType.CREATURE)).prepareSpell()).isNull();
    }

    /**
     * CardView carries a Lombok builder for deriving copies; that must not leak into or disturb
     * the JSON the client actually receives, which is still driven by the record components.
     */
    @Test
    @DisplayName("The prepare spell is serialised as a nested card object")
    void prepareSpellSerialisesAsNestedObject() {
        String json = JsonMapper.builder().build()
                .writeValueAsString(factory.create(prepareCard()));

        assertThat(json).contains("\"prepareSpell\":{");
        assertThat(json).contains("\"name\":\"Heroic Stanza\"");
        assertThat(json).doesNotContain("builder");
    }

    /**
     * Views are derived by copying, so a component added to CardView must survive every
     * derivation. Hand-listed constructor calls used to drop whatever was added last.
     */
    @Test
    @DisplayName("Deriving a view with granted subtypes preserves the prepare spell")
    void derivedViewsPreserveThePrepareSpell() {
        CardView derived = factory.create(prepareCard(), List.of(CardSubtype.WIZARD));

        assertThat(derived.subtypes()).contains(CardSubtype.WIZARD);
        assertThat(derived.prepareSpell()).isNotNull();
        assertThat(derived.prepareSpell().name()).isEqualTo("Heroic Stanza");
    }

    @Test
    @DisplayName("A creature whose only targets belong to its ETB ability asks for no targets while casting")
    void etbTargetsAreNotCastTimeTargets() {
        Card creature = card("Nekrataal", CardType.CREATURE);
        creature.target(new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(),
                        "Target must be a creature"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DestroyTargetPermanentEffect());

        assertThat(factory.create(creature).needsTarget()).isFalse();
    }
}
