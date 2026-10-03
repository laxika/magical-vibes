package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MonssGoblinRaiders;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DespoilerOfSouls.class, GrizzlyBears.class, MonssGoblinRaiders.class, Swamp.class})
class DespoilerOfSoulsTest extends BaseCardTest {

    @Test
    @DisplayName("Despoiler of Souls cannot be declared as a blocker")
    void cannotBlock() {
        addCreatureReady(player2, new DespoilerOfSouls());

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Graveyard ability returns Despoiler of Souls to the battlefield, exiling two other creature cards")
    void graveyardAbilityReturnsSelfToBattlefield() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new MonssGoblinRaiders(), new DespoilerOfSouls()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateGraveyardAbility(player1, 2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Despoiler of Souls");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Mons's Goblin Raiders"))
                .noneMatch(c -> c.getName().equals("Despoiler of Souls"));
    }

    @Test
    @DisplayName("Graveyard ability cannot be activated without two other creature cards to exile")
    void graveyardAbilityRequiresTwoOtherCreatures() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new DespoilerOfSouls()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard to exile");

        harness.assertInGraveyard(player1, "Despoiler of Souls");
    }

    @Test
    @DisplayName("The exile cost is paid before the return ability resolves")
    void exileCostIsPaidOnActivation() {
        GrizzlyBears bears = new GrizzlyBears();
        MonssGoblinRaiders raiders = new MonssGoblinRaiders();
        DespoilerOfSouls despoiler = new DespoilerOfSouls();
        harness.setGraveyard(player1, List.of(bears, raiders, despoiler));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateGraveyardAbility(player1, 2);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(despoiler);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(bears, raiders);
        harness.assertNotOnBattlefield(player1, "Despoiler of Souls");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Despoiler of Souls");
        assertThat(findPermanent(player1, "Despoiler of Souls").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Despoiler of Souls").isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Noncreature cards cannot pay the graveyard exile cost")
    void noncreatureCardsDoNotCountForExileCost() {
        GrizzlyBears bears = new GrizzlyBears();
        Swamp swamp = new Swamp();
        DespoilerOfSouls despoiler = new DespoilerOfSouls();
        harness.setGraveyard(player1, List.of(bears, swamp, despoiler));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard to exile");

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears, swamp, despoiler);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Insufficient black mana prevents activation without exiling cards")
    void insufficientManaDoesNotExileCards() {
        GrizzlyBears bears = new GrizzlyBears();
        MonssGoblinRaiders raiders = new MonssGoblinRaiders();
        DespoilerOfSouls despoiler = new DespoilerOfSouls();
        harness.setGraveyard(player1, List.of(bears, raiders, despoiler));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 2))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears, raiders, despoiler);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The player chooses exile payments and only the activating Despoiler returns")
    void choosesExilePaymentAndReturnsOnlySource() {
        GrizzlyBears bears = new GrizzlyBears();
        MonssGoblinRaiders raiders = new MonssGoblinRaiders();
        DespoilerOfSouls otherDespoiler = new DespoilerOfSouls();
        DespoilerOfSouls source = new DespoilerOfSouls();
        harness.setGraveyard(player1, List.of(bears, raiders, otherDespoiler, source));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateGraveyardAbility(player1, 3);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), raiders.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(bears, raiders);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherDespoiler);
        assertThat(findPermanents(player1, "Despoiler of Souls"))
                .singleElement().satisfies(permanent -> assertThat(permanent.getCard()).isSameAs(source));
    }
}
