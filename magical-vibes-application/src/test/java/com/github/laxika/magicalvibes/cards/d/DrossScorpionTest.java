package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AuriokTransfixer;
import com.github.laxika.magicalvibes.cards.b.BarterInBlood;
import com.github.laxika.magicalvibes.cards.e.ElectrostaticBolt;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        DrossScorpion.class,
        BarterInBlood.class,
        ElectrostaticBolt.class,
        LeoninScimitar.class,
        YotianSoldier.class,
        AuriokTransfixer.class,
        Shatter.class
})
class DrossScorpionTest extends BaseCardTest {

    @Test
    @DisplayName("When Dross Scorpion dies, it may untap a target artifact")
    void selfDeathMayUntapTargetArtifact() {
        harness.addToBattlefield(player1, new DrossScorpion());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        artifact.tap();

        killDrossScorpion();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(artifact.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("When Dross Scorpion dies with no artifact available, its trigger has no legal target")
    void selfDeathWithoutArtifactTargetDoesNotGoOnStack() {
        harness.addToBattlefield(player1, new DrossScorpion());

        killDrossScorpion();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("When another artifact creature dies, Dross Scorpion may untap a target artifact")
    void anotherArtifactCreatureDeathMayUntapTargetArtifact() {
        harness.addToBattlefield(player1, new DrossScorpion());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        artifact.tap();
        harness.addToBattlefield(player2, new YotianSoldier());

        destroyCreatureControlledByPlayer2("Yotian Soldier");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Dross Scorpion does not trigger when a non-artifact creature dies")
    void doesNotTriggerForNonArtifactCreature() {
        harness.addToBattlefield(player1, new DrossScorpion());
        harness.addToBattlefield(player2, new AuriokTransfixer());

        destroyCreatureControlledByPlayer2("Auriok Transfixer");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Dross Scorpion does not trigger when a noncreature artifact dies")
    void doesNotTriggerForNoncreatureArtifact() {
        harness.addToBattlefield(player1, new DrossScorpion());
        destroyArtifactControlledByPlayer2();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining the may ability leaves the target artifact tapped")
    void decliningMayAbilityDoesNotUntapTargetArtifact() {
        harness.addToBattlefield(player1, new DrossScorpion());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        artifact.tap();
        harness.addToBattlefield(player2, new YotianSoldier());

        destroyCreatureControlledByPlayer2("Yotian Soldier");

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(artifact.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The trigger offers artifacts but not non-artifact permanents as targets")
    void targetChoiceOnlyOffersArtifacts() {
        harness.addToBattlefield(player1, new DrossScorpion());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        Permanent nonArtifact = harness.addToBattlefieldAndReturn(player2, new AuriokTransfixer());

        killDrossScorpion();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(artifact.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(nonArtifact.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("Triggers once for itself and once for another artifact creature dying simultaneously")
    void triggersForSelfAndAnotherArtifactCreatureDyingSimultaneously() {
        harness.addToBattlefield(player1, new DrossScorpion());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        artifact.tap();
        harness.addToBattlefield(player1, new YotianSoldier());
        harness.addToBattlefield(player2, new AuriokTransfixer());
        harness.addToBattlefield(player2, new AuriokTransfixer());

        harness.castFromHand(player1, new BarterInBlood(), "{2}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifact.getId());

        for (int i = 0; i < 2; i++) {
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(artifact.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void killDrossScorpion() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ElectrostaticBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Dross Scorpion"));
    }

    private void destroyCreatureControlledByPlayer2(String cardName) {
        harness.setHand(player1, List.of(new ElectrostaticBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, cardName));
    }

    private void destroyArtifactControlledByPlayer2() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, artifact.getId());
    }
}
