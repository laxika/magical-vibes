package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AncientGrudge;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({DevotedGrafkeeper.class, DepartedSoulkeeper.class, AirElemental.class, AncientGrudge.class,
        Forest.class, FountainOfYouth.class, GrizzlyBears.class})
class DevotedGrafkeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and mills two cards")
    void entersAndMillsTwoCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new DevotedGrafkeeper()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Casting a spell from the graveyard taps an opposing creature")
    void graveyardSpellCastTapsOpposingCreature() {
        harness.addToBattlefield(player1, new DevotedGrafkeeper());
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFlashback(player1, 0, fountain.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Disturb casts Devoted Grafkeeper transformed as Departed Soulkeeper")
    void disturbEntersTransformed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new DevotedGrafkeeper()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent soulkeeper = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(soulkeeper.isTransformed()).isTrue();
        assertThat(soulkeeper.getCard().getName()).isEqualTo("Departed Soulkeeper");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Departed Soulkeeper can block flying but not nonflying creatures")
    void backFaceBlocksOnlyFlyingCreatures() {
        Permanent soulkeeper = new Permanent(new DepartedSoulkeeper());
        soulkeeper.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(soulkeeper);

        Permanent flyingAttacker = new Permanent(new AirElemental());
        flyingAttacker.setSummoningSick(false);
        flyingAttacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(flyingAttacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(soulkeeper.isBlocking()).isTrue();

        Permanent groundAttacker = new Permanent(new GrizzlyBears());
        groundAttacker.setSummoningSick(false);
        groundAttacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerBattlefields.get(player1.getId()).add(groundAttacker);
        soulkeeper.setBlocking(false);

        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    @DisplayName("Departed Soulkeeper is exiled instead of going to the graveyard")
    void backFaceIsExiledInsteadOfGraveyard() {
        Permanent soulkeeper = new Permanent(new DepartedSoulkeeper());
        soulkeeper.setTransformed(true);
        soulkeeper.setCard(new DepartedSoulkeeper());
        gd.playerBattlefields.get(player1.getId()).add(soulkeeper);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, soulkeeper));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId()))
                .contains(soulkeeper.getOriginalCard().getId());
    }
}
