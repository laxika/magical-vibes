package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EncrustTest extends BaseCardTest {

    @Test
    @DisplayName("Encrust can enchant a creature")
    void canTargetCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Encrust()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Encrust")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Encrust can enchant an artifact")
    void canTargetArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        Permanent artifact = findPermanent(player2, "Fountain of Youth");

        harness.setHand(player1, List.of(new Encrust()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Encrust")
                        && p.isAttached()
                        && p.getAttachedTo().equals(artifact.getId()));
    }

    @Test
    @DisplayName("Encrust cannot enchant a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Plains());
        Permanent land = findPermanent(player2, "Plains");

        harness.setHand(player1, List.of(new Encrust()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();

        attachEncrust(creature);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted artifact does not untap during its controller's untap step")
    void enchantedArtifactDoesNotUntap() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        Permanent artifact = findPermanent(player2, "Fountain of Youth");
        artifact.tap();

        attachEncrust(artifact);

        advanceToNextTurn(player1);

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature cannot activate its abilities")
    void enchantedCreatureCannotActivateAbilities() {
        Permanent gnomes = new Permanent(new BottleGnomes());
        gnomes.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(gnomes);

        attachEncrustFor(player2, gnomes);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Enchanted artifact cannot activate its abilities")
    void enchantedArtifactCannotActivateAbilities() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        attachEncrustFor(player2, artifact);

        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Creature untaps and can activate abilities again once Encrust leaves")
    void restrictionsEndWhenEncrustRemoved() {
        Permanent gnomes = new Permanent(new BottleGnomes());
        gnomes.setSummoningSick(false);
        gnomes.tap();
        gd.playerBattlefields.get(player1.getId()).add(gnomes);

        Permanent encrust = attachEncrustFor(player2, gnomes);
        gd.playerBattlefields.get(player2.getId()).remove(encrust);

        advanceToNextTurn(player2);

        assertThat(gnomes.isTapped()).isFalse();
        harness.activateAbility(player1, 0, null, null);
    }

    private Permanent attachEncrust(Permanent host) {
        return attachEncrustFor(player1, host);
    }

    private Permanent attachEncrustFor(Player controller, Permanent host) {
        Permanent encrust = new Permanent(new Encrust());
        encrust.setAttachedTo(host.getId());
        gd.playerBattlefields.get(controller.getId()).add(encrust);
        return encrust;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
