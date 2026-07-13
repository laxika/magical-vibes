package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DinOfTheFireherdTest extends BaseCardTest {

    // ===== Token creation =====

    @Test
    @DisplayName("Creates a 5/5 black and red Elemental token under the caster's control")
    void createsElementalToken() {
        castDin(player2.getId());

        Permanent token = findPermanent(player1, "Elemental");
        assertThat(token).isNotNull();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(5);
    }

    // ===== Sacrifice scaling =====

    @Test
    @DisplayName("The token alone (black and red) forces one creature and one land sacrifice")
    void tokenAloneForcesOneCreatureAndOneLand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Swamp());

        castDin(player2.getId());

        // Token counts as both a black and a red creature you control -> 1 creature + 1 land.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Swamp"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Swamp"));
    }

    @Test
    @DisplayName("Extra black and red creatures increase the number of sacrifices")
    void scalesWithBlackAndRedCreatures() {
        harness.addToBattlefield(player1, new ScatheZombies()); // black creature
        harness.addToBattlefield(player1, new HillGiant());     // red creature

        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Forest());

        castDin(player2.getId());

        // Black creatures you control = ScatheZombies + token = 2 -> 2 creatures sacrificed.
        // Red creatures you control = HillGiant + token = 2 -> 2 lands sacrificed.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Swamp"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Opponent chooses which creatures to sacrifice when they have more than required")
    void opponentChoosesWhenMoreCreaturesThanRequired() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Swamp());

        castDin(player2.getId());

        // Only the token is black -> sacrifice exactly 1 of the 2 creatures (opponent's choice).
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        Permanent chosen = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        harness.handleMultiplePermanentsChosen(player2, List.of(chosen.getId()));

        // One creature remains; the land is auto-sacrificed by the red count of 1 (the token).
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears"))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Swamp"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Swamp"));
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Cannot target yourself — must target an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new DinOfTheFireherd()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private void castDin(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new DinOfTheFireherd()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
