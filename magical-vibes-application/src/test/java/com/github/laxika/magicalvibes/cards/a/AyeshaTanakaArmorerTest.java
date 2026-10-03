package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BlackManaBattery;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AyeshaTanakaArmorer.class, BlackManaBattery.class, GrizzlyBears.class, MindStone.class,
        Ornithopter.class, Unsummon.class})
class AyeshaTanakaArmorerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts any number of eligible artifacts onto the battlefield tapped")
    void attackTriggerPutsEligibleArtifactsTapped() {
        addReadyAyesha();
        MindStone mindStone = new MindStone();
        Ornithopter ornithopter = new Ornithopter();
        BlackManaBattery tooExpensive = new BlackManaBattery();
        GrizzlyBears nonArtifact = new GrizzlyBears();
        harness.setLibrary(player1, List.of(mindStone, ornithopter, tooExpensive, nonArtifact));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(mindStone.getId(), ornithopter.getId());
        harness.handleMultipleCardsChosen(player1, List.of(mindStone.getId(), ornithopter.getId()));

        Permanent enteredMindStone = findPermanent(player1, "Mind Stone");
        Permanent enteredOrnithopter = findPermanent(player1, "Ornithopter");
        assertThat(enteredMindStone.isTapped()).isTrue();
        assertThat(enteredOrnithopter.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(tooExpensive, nonArtifact);
    }

    @Test
    @DisplayName("Ayesha can't be blocked when the defending player controls three artifacts")
    void cantBeBlockedWithThreeDefendingArtifacts() {
        Permanent ayesha = addReadyAyesha();
        harness.addToBattlefield(player2, new MindStone());
        harness.addToBattlefield(player2, new MindStone());
        harness.addToBattlefield(player2, new MindStone());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(ayesha)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Ayesha can be blocked when the defending player controls fewer than three artifacts")
    void canBeBlockedWithFewerThanThreeDefendingArtifacts() {
        Permanent ayesha = addReadyAyesha();
        harness.addToBattlefield(player2, new MindStone());
        harness.addToBattlefield(player2, new MindStone());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(ayesha))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The controller may decline all eligible artifacts and only the top four cards move")
    void mayChooseNoArtifacts() {
        addReadyAyesha();
        MindStone stone = new MindStone();
        Ornithopter thopter = new Ornithopter();
        GrizzlyBears bears = new GrizzlyBears();
        BlackManaBattery battery = new BlackManaBattery();
        MindStone fifth = new MindStone();
        harness.setLibrary(player1, List.of(stone, thopter, bears, battery, fifth));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(fifth);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 5))
                .containsExactlyInAnyOrder(stone, thopter, bears, battery);
    }

    @Test
    @DisplayName("Eligibility uses Ayesha's current power when the attack trigger resolves")
    void usesPowerAtResolution() {
        Permanent ayesha = addReadyAyesha();
        BlackManaBattery battery = new BlackManaBattery();
        harness.setLibrary(player1, List.of(battery));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        ayesha.setPowerModifier(2);

        harness.passBothPriorities();
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(battery.getId());
        harness.handleMultipleCardsChosen(player1, List.of(battery.getId()));

        assertThat(findPermanent(player1, "Black Mana Battery").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The controller may choose only some eligible artifacts")
    void mayChooseOnlySomeArtifacts() {
        addReadyAyesha();
        MindStone stone = new MindStone();
        Ornithopter thopter = new Ornithopter();
        harness.setLibrary(player1, List.of(stone, thopter));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(stone.getId()));

        assertThat(findPermanent(player1, "Mind Stone").isTapped()).isTrue();
        assertThat(findPermanents(player1, "Ornithopter")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(thopter);
    }

    @Test
    @DisplayName("The attack trigger uses Ayesha's last known power after she leaves the battlefield")
    void usesLastKnownPowerAfterLeavingBattlefield() {
        Permanent ayesha = addReadyAyesha();
        ayesha.setPowerModifier(2);
        BlackManaBattery battery = new BlackManaBattery();
        harness.setLibrary(player1, List.of(battery));
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        harness.castAndResolveInstant(player1, 0, ayesha.getId());
        assertThat(gd.playerHands.get(player1.getId())).contains(ayesha.getCard());

        resolveAllTriggers();
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(battery.getId());
        harness.handleMultipleCardsChosen(player1, List.of(battery.getId()));

        assertThat(findPermanent(player1, "Black Mana Battery").isTapped()).isTrue();
    }

    @Test
    @DisplayName("No eligible artifacts leaves the library intact apart from bottoming the looked-at cards")
    void noEligibleArtifacts() {
        addReadyAyesha();
        BlackManaBattery battery = new BlackManaBattery();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(battery, bears));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(battery, bears);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addReadyAyesha() {
        return addCreatureReady(player1, new AyeshaTanakaArmorer());
    }
}
