package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BileVialBoggart.class, GrizzlyBears.class, Forest.class, WrathOfGod.class, Shock.class})
class BileVialBoggartTest extends BaseCardTest {

    private void setupCombatWhereBoggartDies() {
        Permanent boggart = findPermanent(player1, "Bile-Vial Boggart");
        boggart.setSummoningSick(false);
        boggart.setAttacking(true);

        GrizzlyBears blocker = new GrizzlyBears();
        blocker.setPower(3);
        blocker.setToughness(3);
        Permanent blockerPermanent = addCreatureReady(player2, blocker);
        blockerPermanent.setBlocking(true);
        blockerPermanent.addBlockingTarget(0);
    }

    @Test
    @DisplayName("When Bile-Vial Boggart dies, it can put a -1/-1 counter on a creature")
    void deathTriggerPutsCounterOnChosenCreature() {
        harness.addToBattlefield(player1, new BileVialBoggart());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereBoggartDies();
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Grizzly Bears");
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The death trigger may choose no target")
    void deathTriggerMayChooseNoTarget() {
        harness.addToBattlefield(player1, new BileVialBoggart());
        harness.addToBattlefield(player2, new GrizzlyBears());
        setupCombatWhereBoggartDies();
        resolveCombat();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPlayerIds()).containsExactly(player1.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Grizzly Bears");
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The death trigger only offers creatures as targets")
    void deathTriggerOnlyOffersCreatures() {
        harness.addToBattlefield(player1, new BileVialBoggart());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID landId = harness.getPermanentId(player2, "Forest");

        setupCombatWhereBoggartDies();
        resolveCombat();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(creatureId).doesNotContain(landId);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The death trigger can target its controller's creature")
    void deathTriggerCanTargetOwnCreature() {
        harness.addToBattlefield(player1, new BileVialBoggart());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        setupCombatWhereBoggartDies();
        resolveCombat();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("A death trigger whose target leaves does not put a counter on another creature")
    void deathTriggerDoesNotRetargetAfterTargetLeaves() {
        harness.addToBattlefield(player1, new BileVialBoggart());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        setupCombatWhereBoggartDies();
        resolveCombat();
        harness.handlePermanentChosen(player1, target.getId());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanent(player2, "Grizzly Bears").getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The death trigger still resolves with no creature to target")
    void deathTriggerResolvesWithoutLegalCreature() {
        harness.addToBattlefield(player1, new BileVialBoggart());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }
}
