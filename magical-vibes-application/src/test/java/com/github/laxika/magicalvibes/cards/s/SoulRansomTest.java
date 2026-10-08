package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FrilledOculus;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulRansom.class, FrilledOculus.class, SimicGuildgate.class, Naturalize.class})
class SoulRansomTest extends BaseCardTest {

    /** Casts Soul Ransom from player1 onto a fresh player2 creature and resolves it. */
    private Permanent stealCreature() {
        Permanent creature = addCreatureReady(player2, new FrilledOculus());
        harness.setHand(player1, List.of(new SoulRansom()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        return creature;
    }

    /** Index of the Soul Ransom permanent on its controller's battlefield. */
    private int auraIndex() {
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        return battlefield.indexOf(findPermanent(player1, "Soul Ransom"));
    }

    @Test
    @DisplayName("Soul Ransom steals the enchanted creature")
    void resolvingStealsCreature() {
        Permanent creature = stealCreature();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("An opponent discards two cards: the Aura's controller sacrifices it and draws two")
    void opponentRansomsTheCreatureBack() {
        Permanent creature = stealCreature();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FrilledOculus(), new SimicGuildgate()));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new SimicGuildgate(), new SimicGuildgate(), new SimicGuildgate()));

        harness.activateAbility(player2, auraIndex(), 0, null, null);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Soul Ransom");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("The Aura's controller may not activate the ransom ability")
    void auraControllerCannotActivate() {
        stealCreature();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new FrilledOculus(), new SimicGuildgate()));

        assertThatThrownBy(() -> harness.activateAbility(player1, auraIndex(), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only your opponents");
    }

    @Test
    @DisplayName("An opponent with fewer than two cards in hand cannot activate the ability")
    void cannotActivateWithoutTwoCards() {
        stealCreature();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SimicGuildgate()));

        assertThatThrownBy(() -> harness.activateAbility(player2, auraIndex(), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Discarding is a cost, but sacrifice and drawing wait for resolution")
    void discardIsPaidBeforeResolution() {
        Permanent creature = stealCreature();
        harness.setHand(player2, List.of(new FrilledOculus(), new SimicGuildgate()));
        harness.setLibrary(player1, List.of(new SimicGuildgate(), new SimicGuildgate()));

        harness.activateAbility(player2, auraIndex(), 0, null, null);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Frilled Oculus");
        harness.assertInGraveyard(player2, "Simic Guildgate");
        harness.assertOnBattlefield(player1, "Soul Ransom");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Soul Ransom");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("The Aura's last controller draws even if it is destroyed in response")
    void drawsAfterAuraLeavesBattlefield() {
        Permanent creature = stealCreature();
        Permanent aura = findPermanent(player1, "Soul Ransom");
        harness.setHand(player2, List.of(new FrilledOculus(), new SimicGuildgate()));
        harness.setHand(player1, List.of(new Naturalize()));
        harness.setLibrary(player1, List.of(new SimicGuildgate(), new SimicGuildgate()));

        harness.activateAbility(player2, auraIndex(), 0, null, null);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, aura.getId());

        harness.assertInGraveyard(player1, "Soul Ransom");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Two ransom activations each draw two cards even though the first sacrifices the Aura")
    void multipleActivationsEachDrawTwo() {
        Permanent creature = stealCreature();
        harness.setHand(player2, List.of(new SimicGuildgate(), new SimicGuildgate(),
                new SimicGuildgate(), new SimicGuildgate()));
        harness.setLibrary(player1, List.of(new SimicGuildgate(), new SimicGuildgate(),
                new SimicGuildgate(), new SimicGuildgate()));

        harness.activateAbility(player2, auraIndex(), 0, null, null);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.activateAbility(player2, auraIndex(), 0, null, null);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Soul Ransom");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Soul Ransom cannot enchant a noncreature permanent")
    void cannotEnchantLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new SimicGuildgate());
        harness.setHand(player1, List.of(new SoulRansom()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
