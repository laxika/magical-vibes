package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OrcishSpy;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RaidingPartyTest extends BaseCardTest {

    @Test
    @DisplayName("Each player taps white creatures and can preserve Plains they do not control")
    void tapsWhiteCreaturesAndPreservesChosenPlains() {
        harness.addToBattlefield(player1, new RaidingParty());
        harness.addToBattlefieldAndReturn(player1, new OrcishSpy());
        Permanent player1White = harness.addToBattlefieldAndReturn(player1, new WhiteKnight());
        Permanent player1NonWhite = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player1Plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent player1OtherPlains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent player2White = harness.addToBattlefieldAndReturn(player2, new WhiteKnight());
        Permanent player2Plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent player2OtherPlains = harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice player1Tap =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player1Tap.playerId()).isEqualTo(player1.getId());
        assertThat(player1Tap.validIds()).containsExactly(player1White.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(player1White.getId()));

        PendingInteraction.MultiPermanentChoice player2Tap =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player2Tap.playerId()).isEqualTo(player2.getId());
        assertThat(player2Tap.validIds()).containsExactly(player2White.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(player2White.getId()));

        PendingInteraction.MultiPermanentChoice player1PlainsChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player1PlainsChoice.playerId()).isEqualTo(player1.getId());
        assertThat(player1PlainsChoice.maxCount()).isEqualTo(2);
        assertThat(player1PlainsChoice.validIds()).containsExactlyInAnyOrder(
                player1Plains.getId(), player1OtherPlains.getId(),
                player2Plains.getId(), player2OtherPlains.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(player2Plains.getId()));

        PendingInteraction.MultiPermanentChoice player2PlainsInteraction =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player2PlainsInteraction.playerId()).isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(player1Plains.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(player1White.isTapped()).isTrue();
        assertThat(player2White.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(player1Plains);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(player2Plains);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(player1NonWhite);
        harness.assertInGraveyard(player1, "Orcish Spy");
        harness.assertInGraveyard(player1, "Plains");
        harness.assertInGraveyard(player2, "Plains");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(player1OtherPlains);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(player2OtherPlains);
    }

    @Test
    @DisplayName("Choosing no white creatures destroys all Plains")
    void choosingNoCreaturesDestroysAllPlains() {
        harness.addToBattlefield(player1, new RaidingParty());
        harness.addToBattlefieldAndReturn(player1, new OrcishSpy());
        Permanent player1White = harness.addToBattlefieldAndReturn(player1, new WhiteKnight());
        Permanent player2White = harness.addToBattlefieldAndReturn(player2, new WhiteKnight());
        harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.handleMultiplePermanentsChosen(player2, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(player1White.isTapped()).isFalse();
        assertThat(player2White.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Plains");
        harness.assertInGraveyard(player2, "Plains");
    }

    @Test
    @DisplayName("White spells cannot target Raiding Party")
    void whiteSpellsCannotTargetIt() {
        Permanent raidingParty = harness.addToBattlefieldAndReturn(player2, new RaidingParty());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, raidingParty.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the target of white spells");
    }
}
