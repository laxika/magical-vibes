package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.h.HistoryOfBenalia;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MishrasSelfReplicator.class, AdelizTheCinderWind.class, GrizzlyBears.class,
        Spellbook.class, HistoryOfBenalia.class})
class MishrasSelfReplicatorTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an artifact triggers may ability prompt")
    void castingArtifactTriggersMayPrompt() {
        harness.addToBattlefield(player1, new MishrasSelfReplicator());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Casting a legendary creature triggers may ability prompt")
    void castingLegendaryTriggersMayPrompt() {
        harness.addToBattlefield(player1, new MishrasSelfReplicator());
        harness.setHand(player1, List.of(new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Casting a non-historic spell does not trigger")
    void nonHistoricDoesNotTrigger() {
        harness.addToBattlefield(player1, new MishrasSelfReplicator());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        // Only the creature spell on the stack, no may prompt
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Opponent casting historic spell does not trigger controller's Self-Replicator")
    void opponentHistoricDoesNotTrigger() {
        harness.addToBattlefield(player1, new MishrasSelfReplicator());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Spellbook()));
        harness.castArtifact(player2, 0);

        // Only artifact spell on stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    @Test
    @DisplayName("Accepting and paying {1} creates a token copy of Self-Replicator")
    void acceptingMayCreatesTokenCopy() {
        harness.addToBattlefield(player1, new MishrasSelfReplicator());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent token = findToken(player1);
        assertThat(token).isNotNull();
        assertThat(token.getCard().getName()).isEqualTo("Mishra's Self-Replicator");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the may ability does not create a token")
    void decliningMayDoesNotCreateToken() {
        harness.addToBattlefield(player1, new MishrasSelfReplicator());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        // No triggered ability on the stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Mishra's Self-Replicator"));

        assertThat(findToken(player1)).isNull();
    }

    @Test
    @DisplayName("Token copy has the same triggered ability as the original")
    void tokenCopyHasSameAbility() {
        harness.addToBattlefield(player1, new MishrasSelfReplicator());
        harness.setHand(player1, List.of(new Spellbook(), new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Cast first artifact and accept may to create a token
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve Spellbook

        Permanent token = findToken(player1);
        assertThat(token).isNotNull();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        assertThat(gd.stack).filteredOn(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).hasSize(3);
    }

    @Test
    @DisplayName("Source leaving battlefield before resolution still creates token via last-known info")
    void sourceLeftBattlefieldStillCreatesToken() {
        harness.addToBattlefield(player1, new MishrasSelfReplicator());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);

        // Remove the source before ability resolves
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Mishra's Self-Replicator") && !p.getCard().isToken());

        harness.passBothPriorities(); // resolve triggered ability
        harness.handleMayAbilityChosen(player1, true);

        // The token uses the source's last-known copiable values.
        assertThat(findToken(player1)).isNotNull();
    }

    @Test
    @DisplayName("Casting a Saga triggers replication")
    void castingSagaTriggersReplication() {
        harness.addToBattlefield(player1, new MishrasSelfReplicator());
        harness.setHand(player1, List.of(new HistoryOfBenalia()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findToken(player1)).isNotNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Payment is chosen only when the triggered ability resolves")
    void paymentWaitsForResolution() {
        harness.addToBattlefield(player1, new MishrasSelfReplicator());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(findToken(player1)).isNotNull();
    }

    @Test
    @DisplayName("Accepting without mana cannot create a token")
    void cannotReplicateWithoutPayment() {
        harness.addToBattlefield(player1, new MishrasSelfReplicator());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findToken(player1)).isNull();
    }

    @Test
    @DisplayName("A single trigger spends only one mana and creates only one copy")
    void singleTriggerCreatesOnlyOneCopy() {
        harness.addToBattlefield(player1, new MishrasSelfReplicator());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    private Permanent findToken(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mishra's Self-Replicator") && p.getCard().isToken())
                .findFirst().orElse(null);
    }
}
