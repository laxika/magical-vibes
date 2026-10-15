package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.s.SkyEelSchool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AcidWebSpider.class, GrizzlyBears.class, LeoninScimitar.class, LoxodonWarhammer.class,
        Memnite.class, SkyEelSchool.class})
class AcidWebSpiderTest extends BaseCardTest {

    /**
     * Resolves Acid Web Spider, chooses the Equipment target, then resolves its trigger
     * and accepts the optional destruction.
     */
    private void castAndAcceptMay(UUID equipmentId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new AcidWebSpider(), "{3}{G}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, equipmentId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    @Test
    @DisplayName("Casting Acid Web Spider puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new AcidWebSpider(), "{3}{G}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Acid Web Spider");
    }

    @Test
    @DisplayName("Resolving puts Acid Web Spider on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new AcidWebSpider(), "{3}{G}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Acid Web Spider");
    }

    @Test
    @DisplayName("Resolving Acid Web Spider triggers may ability prompt when Equipment exists")
    void resolvingTriggersMayPrompt() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.castFromHand(player1, new AcidWebSpider(), "{3}{G}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Leonin Scimitar"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Resolving Acid Web Spider prompts for an Equipment target")
    void resolvingPromptsForTarget() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new AcidWebSpider(), "{3}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Accepting may and choosing Equipment resolves destruction inline")
    void choosingTargetDestroysInline() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        UUID equipmentId = harness.getPermanentId(player2, "Leonin Scimitar");
        castAndAcceptMay(equipmentId);

        // Inner effect resolves inline — Equipment is already destroyed
        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("ETB resolves and destroys target Equipment")
    void etbDestroysTargetEquipment() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        UUID equipmentId = harness.getPermanentId(player2, "Leonin Scimitar");
        castAndAcceptMay(equipmentId);

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Declining may ability does not destroy Equipment")
    void decliningMaySkipsDestruction() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new AcidWebSpider(), "{3}{G}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Leonin Scimitar"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        // Acid Web Spider on battlefield
        harness.assertOnBattlefield(player1, "Acid Web Spider");
        // Equipment still on battlefield
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Can choose which Equipment to destroy when multiple exist")
    void canChooseWhichEquipmentToDestroy() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addToBattlefield(player2, new LoxodonWarhammer());
        UUID warhammerId = harness.getPermanentId(player2, "Loxodon Warhammer");
        castAndAcceptMay(warhammerId);

        // Loxodon Warhammer destroyed
        harness.assertNotOnBattlefield(player2, "Loxodon Warhammer");
        harness.assertInGraveyard(player2, "Loxodon Warhammer");
        // Leonin Scimitar still on battlefield
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("May prompt does not fire when no Equipment on battlefield")
    void noMayPromptWhenNoEquipment() {
        // Without a legal Equipment target, the trigger cannot remain on the stack.
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new AcidWebSpider(), "{3}{G}{G}");
        harness.passBothPriorities(); // resolve creature spell -> enters battlefield

        // No may prompt, nothing waiting on the stack, and the Spider is on the battlefield.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Acid Web Spider");
    }

    @Test
    @DisplayName("ETB does nothing if target Equipment is removed before resolution")
    void etbDoesNothingIfTargetRemoved() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        UUID equipmentId = harness.getPermanentId(player2, "Leonin Scimitar");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new AcidWebSpider(), "{3}{G}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, equipmentId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        // Equipment was already removed from battlefield, graveyard should not contain it
        // (it was removed manually, not destroyed)
        harness.assertNotInGraveyard(player2, "Leonin Scimitar");
        // Acid Web Spider remains on battlefield
        harness.assertOnBattlefield(player1, "Acid Web Spider");
    }

    @Test
    @DisplayName("Can target own Equipment")
    void canTargetOwnEquipment() {
        harness.addToBattlefield(player1, new LeoninScimitar());
        UUID equipmentId = harness.getPermanentId(player1, "Leonin Scimitar");
        castAndAcceptMay(equipmentId);

        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
        harness.assertInGraveyard(player1, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Acid Web Spider remains on battlefield after destroying Equipment")
    void spiderRemainsOnBattlefield() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        UUID equipmentId = harness.getPermanentId(player2, "Leonin Scimitar");
        castAndAcceptMay(equipmentId);

        harness.assertOnBattlefield(player1, "Acid Web Spider");
    }

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterFullResolution() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        UUID equipmentId = harness.getPermanentId(player2, "Leonin Scimitar");
        castAndAcceptMay(equipmentId);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Non-Equipment artifacts do not provide a legal target")
    void noTargetWhenOnlyNonEquipmentArtifactExists() {
        harness.addToBattlefield(player2, new Memnite());

        harness.castFromHand(player1, new AcidWebSpider(), "{3}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Acid Web Spider");
        harness.assertOnBattlefield(player2, "Memnite");
    }

    @Test
    @DisplayName("Reach allows Acid Web Spider to block a flying creature")
    void canBlockFlyingCreature() {
        Permanent spider = addCreatureReady(player2, new AcidWebSpider());
        addCreatureReady(player1, new SkyEelSchool());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spider.isBlocking()).isTrue();
    }
}
