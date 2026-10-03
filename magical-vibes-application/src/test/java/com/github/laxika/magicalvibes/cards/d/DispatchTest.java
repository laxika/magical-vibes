package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BlindZealot;
import com.github.laxika.magicalvibes.cards.p.PorcelainLegionnaire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dispatch.class, BlindZealot.class, PorcelainLegionnaire.class})
class DispatchTest extends BaseCardTest {

    @Test
    @DisplayName("Taps target creature without metalcraft")
    void tapsTargetCreatureWithoutMetalcraft() {
        Permanent creature = addCreatureReady(player2, new BlindZealot());

        harness.setHand(player1, List.of(new Dispatch()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not exile target creature without metalcraft")
    void doesNotExileWithoutMetalcraft() {
        Permanent creature = addCreatureReady(player2, new BlindZealot());

        harness.setHand(player1, List.of(new Dispatch()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Taps and exiles target creature with metalcraft")
    void tapsAndExilesWithMetalcraft() {
        Permanent creature = addCreatureReady(player2, new BlindZealot());

        harness.setHand(player1, List.of(new Dispatch()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        addThreeArtifacts(player1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isNotEmpty();
    }

    @Test
    @DisplayName("Only taps if metalcraft lost before resolution")
    void onlyTapsIfMetalcraftLostBeforeResolution() {
        Permanent creature = addCreatureReady(player2, new BlindZealot());

        harness.setHand(player1, List.of(new Dispatch()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        addThreeArtifacts(player1);

        harness.castInstant(player1, 0, creature.getId());

        // Remove artifacts before resolution
        gd.playerBattlefields.get(player1.getId()).removeIf(
                p -> p.getCard() instanceof PorcelainLegionnaire);

        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiles if metalcraft is gained before resolution")
    void exilesIfMetalcraftGainedBeforeResolution() {
        Permanent creature = addCreatureReady(player2, new BlindZealot());
        harness.setHand(player1, List.of(new Dispatch()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, creature.getId());

        addThreeArtifacts(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature.getCard());
    }

    @Test
    @DisplayName("Exiles an already tapped creature with metalcraft")
    void exilesAlreadyTappedCreature() {
        Permanent creature = addCreatureReady(player2, new BlindZealot());
        creature.setTapped(true);
        addThreeArtifacts(player1);
        harness.setHand(player1, List.of(new Dispatch()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature.getCard());
    }

    @Test
    @DisplayName("Opponent's artifacts do not enable metalcraft")
    void opponentsArtifactsDoNotEnableMetalcraft() {
        Permanent creature = addCreatureReady(player2, new BlindZealot());
        addThreeArtifacts(player2);
        harness.setHand(player1, List.of(new Dispatch()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Two artifacts are insufficient for metalcraft")
    void twoArtifactsOnlyTapTheCreature() {
        Permanent creature = addCreatureReady(player2, new BlindZealot());
        harness.addToBattlefield(player1, new PorcelainLegionnaire());
        harness.addToBattlefield(player1, new PorcelainLegionnaire());
        harness.setHand(player1, List.of(new Dispatch()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A targeted artifact creature counts toward its controller's metalcraft")
    void ownTargetCountsTowardMetalcraft() {
        Permanent creature = addCreatureReady(player1, new PorcelainLegionnaire());
        harness.addToBattlefield(player1, new PorcelainLegionnaire());
        harness.addToBattlefield(player1, new PorcelainLegionnaire());
        harness.setHand(player1, List.of(new Dispatch()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature.getCard());
    }

    @Test
    @DisplayName("Does nothing when the target leaves before resolution")
    void doesNothingWhenTargetLeavesBeforeResolution() {
        Permanent creature = addCreatureReady(player2, new BlindZealot());
        addThreeArtifacts(player1);
        harness.setHand(player1, List.of(new Dispatch()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, creature.getId());

        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerGraveyards.get(player2.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature.getCard());
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void addThreeArtifacts(Player player) {
        harness.addToBattlefield(player, new PorcelainLegionnaire());
        harness.addToBattlefield(player, new PorcelainLegionnaire());
        harness.addToBattlefield(player, new PorcelainLegionnaire());
    }
}
