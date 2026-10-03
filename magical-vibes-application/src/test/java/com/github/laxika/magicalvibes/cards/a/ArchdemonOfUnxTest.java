package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.d.DregscapeZombie;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchdemonOfUnx.class, CylianElf.class, DregscapeZombie.class})
class ArchdemonOfUnxTest extends BaseCardTest {

    private long blackZombieTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.ZOMBIE))
                .filter(p -> p.getCard().getColor() == CardColor.BLACK)
                .filter(p -> p.getCard().getPower() == 2 && p.getCard().getToughness() == 2)
                .count();
    }

    @Test
    @DisplayName("With itself the only non-Zombie creature, it sacrifices itself and creates a 2/2 black Zombie")
    void sacrificesSelfAndCreatesToken() {
        harness.addToBattlefield(player1, new ArchdemonOfUnx());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Archdemon of Unx");
        harness.assertInGraveyard(player1, "Archdemon of Unx");
        assertThat(blackZombieTokens()).isEqualTo(1);
    }

    @Test
    @DisplayName("A Zombie creature is not eligible to be sacrificed")
    void zombieCreatureIsNotSacrificed() {
        harness.addToBattlefield(player1, new ArchdemonOfUnx());
        harness.addToBattlefield(player1, new DregscapeZombie());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Dregscape Zombie");
        harness.assertNotOnBattlefield(player1, "Archdemon of Unx");
        assertThat(blackZombieTokens()).isEqualTo(1);
    }

    @Test
    @DisplayName("With multiple non-Zombie creatures, the controller chooses which one to sacrifice")
    void controllerChoosesWhichNonZombieToSacrifice() {
        harness.addToBattlefield(player1, new ArchdemonOfUnx());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new CylianElf());

        advanceToUpkeep(player1);
        resolveAllTriggers();
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.playerId()).isEqualTo(player1.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(elf.getId()));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Cylian Elf");
        harness.assertInGraveyard(player1, "Cylian Elf");
        harness.assertOnBattlefield(player1, "Archdemon of Unx");
        assertThat(blackZombieTokens()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new ArchdemonOfUnx());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Archdemon of Unx");
        assertThat(blackZombieTokens()).isZero();
    }

    @Test
    @DisplayName("Sacrifice and token creation resolve as one ability, with sacrifice first")
    void createsTokenOnlyAfterSacrificeChoice() {
        harness.addToBattlefield(player1, new ArchdemonOfUnx());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        harness.addToBattlefield(player1, new DregscapeZombie());
        harness.addToBattlefield(player2, new CylianElf());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(blackZombieTokens()).isZero();

        harness.handleMultiplePermanentsChosen(player1, List.of(elf.getId()));

        harness.assertInGraveyard(player1, "Cylian Elf");
        harness.assertOnBattlefield(player1, "Dregscape Zombie");
        harness.assertOnBattlefield(player2, "Cylian Elf");
        assertThat(blackZombieTokens()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creates a Zombie even when no non-Zombie creature remains at resolution")
    void createsTokenWhenNoEligibleCreatureRemains() {
        Permanent archdemon = harness.addToBattlefieldAndReturn(player1, new ArchdemonOfUnx());
        harness.addToBattlefield(player1, new DregscapeZombie());
        harness.addToBattlefield(player2, new CylianElf());

        advanceToUpkeep(player1);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, archdemon);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Dregscape Zombie");
        harness.assertOnBattlefield(player2, "Cylian Elf");
        assertThat(blackZombieTokens()).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Archdemon creates one Zombie and requires its own sacrifice")
    void multipleArchdemonsEachSacrificeAndCreateToken() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ArchdemonOfUnx());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ArchdemonOfUnx());

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(first.getId()) || p.getId().equals(second.getId()));
        assertThat(blackZombieTokens()).isEqualTo(2);
    }
}
