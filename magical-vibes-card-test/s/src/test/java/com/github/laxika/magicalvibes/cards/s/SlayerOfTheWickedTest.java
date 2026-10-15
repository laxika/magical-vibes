package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.a.AbbeyGriffin;
import com.github.laxika.magicalvibes.cards.a.AltarsReap;
import com.github.laxika.magicalvibes.cards.d.DiregrafGhoul;
import com.github.laxika.magicalvibes.cards.v.VampireInterloper;
import com.github.laxika.magicalvibes.cards.v.VillagersOfEstwald;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlayerOfTheWicked.class, DiregrafGhoul.class, VampireInterloper.class,
        VillagersOfEstwald.class, AbbeyGriffin.class, AltarsReap.class})
class SlayerOfTheWickedTest extends BaseCardTest {

    /**
     * Casts Slayer of the Wicked and resolves it onto the battlefield, then accepts the may ability
     * after choosing the target and resolving the ETB triggered ability.
     */
    private void castAndAcceptMay(UUID targetId) {
        harness.castFromHand(player1, new SlayerOfTheWicked(), "{3}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    @Test
    @DisplayName("Casting Slayer of the Wicked puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new SlayerOfTheWicked(), "{3}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(SlayerOfTheWicked.class);
    }

    @Test
    @DisplayName("Resolving puts Slayer of the Wicked on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new SlayerOfTheWicked(), "{3}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Slayer of the Wicked");
    }

    @Test
    @DisplayName("Resolving Slayer triggers may ability prompt when valid target exists")
    void resolvingTriggersMayPrompt() {
        harness.addToBattlefield(player2, new DiregrafGhoul());
        harness.castFromHand(player1, new SlayerOfTheWicked(), "{3}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Diregraf Ghoul"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Putting the ETB ability on the stack prompts for target selection")
    void puttingEtbAbilityOnStackPromptsForTarget() {
        harness.addToBattlefield(player2, new DiregrafGhoul());
        harness.castFromHand(player1, new SlayerOfTheWicked(), "{3}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("ETB destroys target Zombie")
    void etbDestroysTargetZombie() {
        harness.addToBattlefield(player2, new DiregrafGhoul());
        UUID zombieId = harness.getPermanentId(player2, "Diregraf Ghoul");
        castAndAcceptMay(zombieId);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Diregraf Ghoul");
        harness.assertInGraveyard(player2, "Diregraf Ghoul");
    }

    @Test
    @DisplayName("ETB destroys target Vampire")
    void etbDestroysTargetVampire() {
        VampireInterloper vampire = new VampireInterloper();
        harness.addToBattlefield(player2, vampire);
        UUID vampireId = harness.getPermanentId(player2, "Vampire Interloper");
        castAndAcceptMay(vampireId);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Vampire Interloper");
        harness.assertInGraveyard(player2, "Vampire Interloper");
    }

    @Test
    @DisplayName("ETB destroys target Werewolf")
    void etbDestroysTargetWerewolf() {
        VillagersOfEstwald werewolf = new VillagersOfEstwald();
        harness.addToBattlefield(player2, werewolf);
        UUID werewolfId = harness.getPermanentId(player2, "Villagers of Estwald");
        castAndAcceptMay(werewolfId);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Villagers of Estwald");
        harness.assertInGraveyard(player2, "Villagers of Estwald");
    }

    @Test
    @DisplayName("Declining may ability does not destroy target")
    void decliningMaySkipsDestruction() {
        harness.addToBattlefield(player2, new DiregrafGhoul());
        harness.castFromHand(player1, new SlayerOfTheWicked(), "{3}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Diregraf Ghoul"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Slayer of the Wicked");
        harness.assertOnBattlefield(player2, "Diregraf Ghoul");
    }

    @Test
    @DisplayName("May prompt does not fire when no valid targets on battlefield")
    void noMayPromptWithoutValidTargets() {
        harness.addToBattlefield(player2, new AbbeyGriffin());
        harness.castFromHand(player1, new SlayerOfTheWicked(), "{3}{W}");
        harness.passBothPriorities(); // resolve creature spell -> enters battlefield

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Slayer of the Wicked");
    }

    @Test
    @DisplayName("Can target own Zombie creature")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new DiregrafGhoul());
        UUID zombieId = harness.getPermanentId(player1, "Diregraf Ghoul");
        castAndAcceptMay(zombieId);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Diregraf Ghoul");
        harness.assertInGraveyard(player1, "Diregraf Ghoul");
    }

    @Test
    @DisplayName("Slayer of the Wicked remains on battlefield after destroying target")
    void slayerRemainsOnBattlefield() {
        harness.addToBattlefield(player2, new DiregrafGhoul());
        UUID zombieId = harness.getPermanentId(player2, "Diregraf Ghoul");
        castAndAcceptMay(zombieId);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Slayer of the Wicked");
    }

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterFullResolution() {
        harness.addToBattlefield(player2, new DiregrafGhoul());
        UUID zombieId = harness.getPermanentId(player2, "Diregraf Ghoul");
        castAndAcceptMay(zombieId);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB destroys only the chosen eligible permanent")
    void destroysOnlyChosenPermanent() {
        harness.addToBattlefield(player2, new DiregrafGhoul());
        harness.addToBattlefield(player2, new VampireInterloper());
        harness.addToBattlefield(player2, new AbbeyGriffin());
        castAndAcceptMay(harness.getPermanentId(player2, "Vampire Interloper"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Vampire Interloper");
        harness.assertOnBattlefield(player2, "Diregraf Ghoul");
        harness.assertOnBattlefield(player2, "Abbey Griffin");
    }

    @Test
    @DisplayName("A target sacrificed in response makes the ETB ability fail without a may prompt")
    void targetLeavingBeforeResolutionSkipsMayChoice() {
        harness.addToBattlefield(player2, new DiregrafGhoul());
        UUID targetId = harness.getPermanentId(player2, "Diregraf Ghoul");
        harness.setHand(player2, List.of(new AltarsReap()));
        harness.setLibrary(player2, List.of(new AbbeyGriffin(), new AbbeyGriffin()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castFromHand(player1, new SlayerOfTheWicked(), "{3}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.castInstantWithSacrifice(player2, 0, null, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Diregraf Ghoul");
        harness.assertOnBattlefield(player1, "Slayer of the Wicked");
    }
}
