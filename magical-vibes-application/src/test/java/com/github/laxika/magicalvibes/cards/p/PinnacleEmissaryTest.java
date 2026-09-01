package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PinnacleEmissary.class, Spellbook.class, GrizzlyBears.class, SuntailHawk.class})
class PinnacleEmissaryTest extends BaseCardTest {

    @Test
    void createsAFlyingDroneWhenYouCastAnArtifactSpell() {
        harness.addToBattlefield(player1, new PinnacleEmissary());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 0);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent drone = findPermanent(player1, "Drone");
        assertThat(drone.getCard().getColor()).isNull();
        assertThat(drone.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(drone.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(drone.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(gqs.getEffectivePower(gd, drone)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drone)).isEqualTo(1);
    }

    @Test
    void doesNotCreateADroneForANonartifactSpell() {
        harness.addToBattlefield(player1, new PinnacleEmissary());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Drone"));
    }

    @Test
    void droneCanBlockOnlyACreatureWithFlying() {
        harness.addToBattlefield(player2, new PinnacleEmissary());
        harness.setHand(player2, List.of(new Spellbook()));
        harness.addMana(player2, ManaColor.COLORLESS, 0);
        harness.forceActivePlayer(player2);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent drone = findPermanent(player2, "Drone");
        Permanent groundAttacker = addReadyAttacker(player1, new GrizzlyBears());
        prepareDeclareBlockers(player1);

        int droneIndex = gd.playerBattlefields.get(player2.getId()).indexOf(drone);
        int groundAttackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(groundAttacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(droneIndex, groundAttackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creatures with flying");

        Permanent flyingAttacker = addReadyAttacker(player1, new SuntailHawk());
        int flyingAttackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(flyingAttacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(droneIndex, flyingAttackerIndex)));

        assertThat(drone.isBlocking()).isTrue();
    }

    @Test
    void canBeWarpCastAndExilesAtTheNextEndStep() {
        PinnacleEmissary emissary = new PinnacleEmissary();
        harness.setHand(player1, List.of(emissary));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard() instanceof PinnacleEmissary);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(emissary.getId())).isNotNull();
    }

    private Permanent addReadyAttacker(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
