package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CascadeBluffs;
import com.github.laxika.magicalvibes.cards.d.DuskdaleWurm;
import com.github.laxika.magicalvibes.cards.l.LeeringEmblem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BanishingKnack.class, CascadeBluffs.class, DuskdaleWurm.class, LeeringEmblem.class})
class BanishingKnackTest extends BaseCardTest {

    private void endTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    /** Casts Banishing Knack on player1's ready creature, granting it the bounce ability. */
    private Permanent grantAbilityToReadyCreature() {
        Permanent creature = addCreatureReady(player1, new DuskdaleWurm());

        harness.setHand(player1, List.of(new BanishingKnack()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        return creature;
    }

    @Test
    @DisplayName("Target creature gains the granted ability, which returns a nonland permanent to its owner's hand")
    void grantedAbilityBouncesNonlandPermanent() {
        Permanent creature = grantAbilityToReadyCreature();
        Permanent bounceTarget = harness.addToBattlefieldAndReturn(player2, new DuskdaleWurm());

        harness.activateAbility(player1, 0, null, bounceTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bounceTarget);
        harness.assertInHand(player2, "Duskdale Wurm");
        // Using the tap ability taps the creature it was granted to.
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The granted ability can return a noncreature nonland permanent")
    void grantedAbilityBouncesNoncreatureNonlandPermanent() {
        grantAbilityToReadyCreature();
        Permanent bounceTarget = harness.addToBattlefieldAndReturn(player2, new LeeringEmblem());

        harness.activateAbility(player1, 0, null, bounceTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bounceTarget);
        harness.assertInHand(player2, "Leering Emblem");
    }

    @Test
    @DisplayName("Granted ability cannot target a land")
    void grantedAbilityCannotTargetLand() {
        grantAbilityToReadyCreature();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new CascadeBluffs());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("The granted ability wears off at end of turn")
    void grantedAbilityWearsOffAtEndOfTurn() {
        Permanent creature = grantAbilityToReadyCreature();
        harness.addToBattlefieldAndReturn(player2, new DuskdaleWurm());

        endTurn();
        creature.setSummoningSick(false);

        UUID bounceTarget = harness.getPermanentId(player2, "Duskdale Wurm");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bounceTarget))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Banishing Knack cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new DuskdaleWurm()); // valid target so spell is playable
        Permanent land = harness.addToBattlefieldAndReturn(player2, new CascadeBluffs());

        harness.setHand(player1, List.of(new BanishingKnack()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
    @Test
    @DisplayName("The granted ability can return its own source to hand")
    void grantedAbilityCanBounceItself() {
        Permanent creature = grantAbilityToReadyCreature();

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        harness.assertInHand(player1, "Duskdale Wurm");
    }

    @Test
    @DisplayName("An opponent's creature can gain the ability and its controller can activate it")
    void opponentsCreatureCanGainAndActivateAbility() {
        Permanent creature = addCreatureReady(player2, new DuskdaleWurm());
        Permanent bounceTarget = harness.addToBattlefieldAndReturn(player1, new LeeringEmblem());
        harness.setHand(player1, List.of(new BanishingKnack()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.ensurePriority(player2);
        harness.activateAbility(player2, 0, null, bounceTarget.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bounceTarget);
        harness.assertInHand(player1, "Leering Emblem");
    }

    @Test
    @DisplayName("Granting the tap ability does not bypass summoning sickness")
    void summoningSickCreatureCannotActivateGrantedAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DuskdaleWurm());
        Permanent bounceTarget = harness.addToBattlefieldAndReturn(player2, new LeeringEmblem());
        harness.setHand(player1, List.of(new BanishingKnack()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bounceTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bounceTarget);
    }
}
