package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GildedLotus;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Encrust.class, BottleGnomes.class, FountainOfYouth.class, GrizzlyBears.class, Plains.class, GildedLotus.class})
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
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

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
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());

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

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted artifact does not untap during its controller's untap step")
    void enchantedArtifactDoesNotUntap() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        artifact.tap();

        attachEncrust(artifact);

        harness.performUntapStep(player2);

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature cannot activate its abilities")
    void enchantedCreatureCannotActivateAbilities() {
        Permanent gnomes = addCreatureReady(player1, new BottleGnomes());

        attachEncrustFor(player2, gnomes);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Enchanted artifact cannot activate its abilities")
    void enchantedArtifactCannotActivateAbilities() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        attachEncrustFor(player2, artifact);

        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Creature untaps and can activate abilities again once Encrust leaves")
    void restrictionsEndWhenEncrustRemoved() {
        Permanent gnomes = addCreatureReady(player1, new BottleGnomes());
        gnomes.tap();

        Permanent encrust = attachEncrustFor(player2, gnomes);
        gd.playerBattlefields.get(player2.getId()).remove(encrust);

        harness.performUntapStep(player1);

        assertThat(gnomes.isTapped()).isFalse();
        harness.activateAbility(player1, 0, null, null);
    }

    @Test
    @DisplayName("Encrust does not tap an untapped permanent when it resolves")
    void doesNotTapOnResolution() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new Encrust()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Encrust").getAttachedTo()).isEqualTo(artifact.getId());
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Encrust prevents activation of mana abilities")
    void enchantedArtifactCannotActivateManaAbility() {
        Permanent lotus = harness.addToBattlefieldAndReturn(player1, new GildedLotus());
        attachEncrustFor(player2, lotus);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThat(lotus.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Encrust does not prevent other permanents from untapping")
    void otherPermanentsUntapNormally() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent other = addCreatureReady(player2, new GrizzlyBears());
        enchanted.tap();
        other.tap();
        attachEncrust(enchanted);

        harness.performUntapStep(player2);

        assertThat(enchanted.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
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

}
