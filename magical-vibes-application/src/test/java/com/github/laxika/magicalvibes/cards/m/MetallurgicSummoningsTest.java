package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AppetiteForTheUnnatural;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MetallurgicSummonings.class, Divination.class, GrizzlyBears.class, Shock.class,
        PropheticPrism.class, AppetiteForTheUnnatural.class})
class MetallurgicSummoningsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant or sorcery creates a Construct token with that spell's mana value")
    void instantOrSorceryCreatesConstructWithSpellManaValue() {
        harness.addToBattlefield(player1, new MetallurgicSummonings());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent construct = findPermanent(player1, "Construct");
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(3);
        assertThat(construct.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(construct.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Exiling the enchantment returns all instants and sorceries from the graveyard")
    void activationReturnsAllInstantsAndSorceries() {
        harness.addToBattlefield(player1, new MetallurgicSummonings());
        addArtifacts(player1, 6);

        Card shock = new Shock();
        Card divination = new Divination();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(shock, divination, creature));
        harness.addMana(player1, ManaColor.BLUE, 5);

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanent(player1, "Metallurgic Summonings"));
        harness.activateAbility(player1, sourceIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(shock.getId(), divination.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactly(creature.getId());
        assertThat(countPermanents(player1, "Metallurgic Summonings")).isZero();
    }

    @Test
    @DisplayName("The recursion ability cannot be activated without six artifacts")
    void activationRequiresSixArtifacts() {
        harness.addToBattlefield(player1, new MetallurgicSummonings());
        addArtifacts(player1, 5);
        harness.addMana(player1, ManaColor.BLUE, 5);

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanent(player1, "Metallurgic Summonings"));
        assertThatThrownBy(() -> harness.activateAbility(player1, sourceIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addArtifacts(com.github.laxika.magicalvibes.model.Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new PropheticPrism());
        }
    }

    @Test
    void instantCreatesOneConstructBeforeTheSpellResolves() {
        harness.addToBattlefield(player1, new MetallurgicSummonings());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(countPermanents(player1, "Construct")).isEqualTo(1);
        Permanent construct = findPermanent(player1, "Construct");
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(1);
        assertThat(construct.getCard().getColors()).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);

        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void opponentsInstantDoesNotCreateAConstruct() {
        harness.addToBattlefield(player1, new MetallurgicSummonings());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(countPermanents(player1, "Construct")).isZero();
        assertThat(countPermanents(player2, "Construct")).isZero();
    }

    @Test
    void triggerStillCreatesConstructAfterEnchantmentIsDestroyed() {
        Card source = new MetallurgicSummonings();
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, source);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new AppetiteForTheUnnatural()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player2, 0, enchantment.getId());

        assertThat(countPermanents(player1, "Metallurgic Summonings")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Construct")).isEqualTo(1);
        Permanent construct = findPermanent(player1, "Construct");
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(1);
        assertThat(countPermanents(player2, "Construct")).isZero();
    }

    @Test
    void artifactSpellDoesNotCreateAConstruct() {
        harness.addToBattlefield(player1, new MetallurgicSummonings());
        harness.setHand(player1, List.of(new PropheticPrism()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Construct")).isZero();
        assertThat(countPermanents(player1, "Prophetic Prism")).isEqualTo(1);
    }

    @Test
    void opponentsArtifactsDoNotSatisfyActivationRestriction() {
        harness.addToBattlefield(player1, new MetallurgicSummonings());
        addArtifacts(player1, 5);
        addArtifacts(player2, 6);
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Metallurgic Summonings")).isEqualTo(1);
    }

    @Test
    void activationExilesImmediatelyAndStillResolvesAfterLosingAnArtifact() {
        Card source = new MetallurgicSummonings();
        Card instant = new AppetiteForTheUnnatural();
        Card opponentsInstant = new AppetiteForTheUnnatural();
        harness.addToBattlefield(player1, source);
        addArtifacts(player1, 6);
        harness.setGraveyard(player1, List.of(instant));
        harness.setGraveyard(player2, List.of(opponentsInstant));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.setHand(player2, List.of(new AppetiteForTheUnnatural()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        Permanent artifact = findPermanent(player1, "Prophetic Prism");

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(countPermanents(player1, "Metallurgic Summonings")).isZero();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId()).contains(source.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(instant);

        harness.castAndResolveInstant(player2, 0, artifact.getId());
        assertThat(countPermanents(player1, "Prophetic Prism")).isEqualTo(5);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(instant);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(instant);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentsInstant);
    }

    @Test
    void activationIsLegalWithAnEmptyGraveyard() {
        Card source = new MetallurgicSummonings();
        harness.addToBattlefield(player1, source);
        addArtifacts(player1, 6);
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId()).contains(source.getId());
    }
}
