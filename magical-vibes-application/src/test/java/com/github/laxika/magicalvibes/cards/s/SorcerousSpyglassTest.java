package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.d.DroverOfTheMighty;
import com.github.laxika.magicalvibes.cards.l.LightningRigCrew;
import com.github.laxika.magicalvibes.cards.t.TreasureMap;
import com.github.laxika.magicalvibes.cards.t.TreasureCove;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SorcerousSpyglass.class, LightningRigCrew.class, DroverOfTheMighty.class, SongOfTheDryads.class, TreasureMap.class, TreasureCove.class})
class SorcerousSpyglassTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Sorcerous Spyglass puts it on the stack as artifact spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new SorcerousSpyglass()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Sorcerous Spyglass");
    }

    @Test
    @DisplayName("Resolving Sorcerous Spyglass reveals opponent's hand and awaits card name choice")
    void resolvingRevealsHandAndTriggersCardNameChoice() {
        Card cardInOpponentHand = new SorcerousSpyglass();
        harness.setHand(player2, List.of(cardInOpponentHand));

        harness.setHand(player1, List.of(new SorcerousSpyglass()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        // The public log records the look without exposing card identities.
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("looks at") && log.contains("hand"));

        harness.assertNotOnBattlefield(player1, "Sorcerous Spyglass");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Resolving with empty opponent hand logs that hand is empty")
    void resolvingWithEmptyHandLogsEmpty() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new SorcerousSpyglass()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("looks at") && log.contains("empty"));
    }

    @Test
    @DisplayName("RevealHandMessage is sent to the controller")
    void revealHandMessageSentToController() {
        Card cardInOpponentHand = new SorcerousSpyglass();
        harness.setHand(player2, List.of(cardInOpponentHand));

        harness.setHand(player1, List.of(new SorcerousSpyglass()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.clearMessages();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        List<String> p1Messages = harness.getConn1().getSentMessages();
        assertThat(p1Messages).anyMatch(msg -> msg.contains("REVEAL_HAND"));
        assertThat(harness.getConn2().getSentMessages()).noneMatch(msg -> msg.contains("REVEAL_HAND"));
    }

    @Test
    @DisplayName("Choosing a card name sets chosenName on the permanent")
    void choosingNameSetsOnPermanent() {
        harness.setHand(player1, List.of(new SorcerousSpyglass()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lightning-Rig Crew");

        Permanent perm = findPermanent(player1, "Sorcerous Spyglass");
        assertThat(perm.getChosenName()).isEqualTo("Lightning-Rig Crew");
    }

    @Test
    @DisplayName("Blocks non-mana activated abilities of the named card")
    void blocksNonManaActivatedAbilities() {
        addReadySpyglass(player1, "Lightning-Rig Crew");

        addCreatureReady(player2, new LightningRigCrew());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Does NOT block mana abilities of the named card")
    void doesNotBlockManaAbilities() {
        addReadySpyglass(player1, "Drover of the Mighty");

        addCreatureReady(player2, new DroverOfTheMighty());

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Does NOT block abilities of differently-named cards")
    void doesNotBlockDifferentlyNamedCards() {
        addReadySpyglass(player1, "Sorcerous Spyglass");

        addCreatureReady(player2, new LightningRigCrew());

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Lightning-Rig Crew");
    }

    @Test
    @DisplayName("After Sorcerous Spyglass leaves the battlefield, abilities are usable again")
    void abilitiesWorkAfterSpyglassRemoved() {
        Permanent spyglass = addReadySpyglass(player1, "Lightning-Rig Crew");

        addCreatureReady(player2, new LightningRigCrew());

        // Verify blocked
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");

        // Remove Sorcerous Spyglass from battlefield
        gd.playerBattlefields.get(player1.getId()).remove(spyglass);

        // Now the ability should work
        harness.activateAbility(player2, 0, null, null);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void namedManaAbilityResolvesWithoutUsingTheStack() {
        addReadySpyglass(player1, "Drover of the Mighty");
        Permanent drover = addCreatureReady(player2, new DroverOfTheMighty());

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "GREEN");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(drover.isTapped()).isTrue();
    }

    @Test
    void blocksControllersOwnNamedCardWithoutPayingCosts() {
        addReadySpyglass(player1, "Lightning-Rig Crew");
        Permanent crew = addCreatureReady(player1, new LightningRigCrew());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");

        assertThat(crew.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void namingSourceDoesNotStopAbilityAlreadyOnStack() {
        addCreatureReady(player2, new LightningRigCrew());
        harness.activateAbility(player2, 0, null, null);
        addReadySpyglass(player1, "Lightning-Rig Crew");
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void namedCardTriggeredAbilityStillUntaps() {
        addReadySpyglass(player1, "Lightning-Rig Crew");
        Permanent crew = addCreatureReady(player1, new LightningRigCrew());
        crew.setTapped(true);
        harness.setHand(player1, List.of(new LightningRigCrew()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void losingPrintedAbilitiesStopsTheNameLock() {
        Permanent spyglass = addReadySpyglass(player1, "Lightning-Rig Crew");
        addCreatureReady(player2, new LightningRigCrew());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, spyglass.getId());
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    void namesAbsentFromOpponentsHandAreAllowedEvenWhenHandIsEmpty() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new SorcerousSpyglass()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Treasure Cove");

        assertThat(findPermanent(player1, "Sorcerous Spyglass").getChosenName()).isEqualTo("Treasure Cove");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void payingManaDoesNotMakeAnAbilityAManaAbility() {
        addReadySpyglass(player1, "Treasure Map");
        Permanent map = harness.addToBattlefieldAndReturn(player2, new TreasureMap());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");

        assertThat(map.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadySpyglass(Player player, String chosenName) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SorcerousSpyglass());
        perm.setChosenName(chosenName);
        return perm;
    }

}
