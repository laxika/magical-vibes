package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.PincherBeetles;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UndercityEliminator.class, Forest.class, GrizzlyBears.class, Ornithopter.class,
        MindStone.class, PincherBeetles.class})
class UndercityEliminatorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may sacrifice an artifact or creature to exile an opponent's creature")
    void etbSacrificeExilesOpponentCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        UndercityEliminator eliminator = new UndercityEliminator();
        harness.setHand(player1, List.of(eliminator));
        addEliminatorMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice sacrificeChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(sacrificeChoice.validIds()).contains(sacrifice.getId());
        harness.handlePermanentChosen(player1, sacrifice.getId());

        PendingInteraction.PermanentChoice targetChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(targetChoice.validIds()).contains(target.getId()).doesNotContain(land.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(eliminator.getId()));
    }

    @Test
    @DisplayName("Declining the ETB sacrifice leaves permanents unchanged")
    void decliningEtbSacrificeDoesNothing() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UndercityEliminator()));
        addEliminatorMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrifice);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("The Eliminator can sacrifice itself and its reflexive ability still exiles a creature")
    void sacrificingItselfStillExilesCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        UndercityEliminator eliminator = new UndercityEliminator();
        harness.setHand(player1, List.of(eliminator));
        addEliminatorMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(eliminator.getId()))
                .findFirst().orElseThrow();
        PendingInteraction.PermanentChoice sacrificeChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(sacrificeChoice.validIds()).contains(source.getId(), ownCreature.getId())
                .doesNotContain(ownLand.getId(), target.getId());
        harness.handlePermanentChosen(player1, source.getId());

        PendingInteraction.PermanentChoice targetChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(targetChoice.validIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(eliminator);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature, ownLand)
                .doesNotContain(source);
    }

    @Test
    @DisplayName("A sacrifice is allowed even when no opposing creature can be targeted")
    void canSacrificeWithoutAnOpponentCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new UndercityEliminator()));
        addEliminatorMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrifice);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A noncreature artifact can be sacrificed but cannot be the exile target")
    void sacrificesNoncreatureArtifact() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UndercityEliminator()));
        addEliminatorMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.PermanentChoice sacrificeChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(sacrificeChoice.validIds()).contains(sacrifice.getId())
                .doesNotContain(opponentArtifact.getId(), target.getId());
        harness.handlePermanentChosen(player1, sacrifice.getId());

        PendingInteraction.PermanentChoice targetChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(targetChoice.validIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentArtifact)
                .doesNotContain(target);
    }

    @Test
    @DisplayName("The reflexive exile trigger cannot target a creature with shroud")
    void shroudCreatureIsNotAnExileTarget() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player2, new PincherBeetles());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UndercityEliminator()));
        addEliminatorMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        PendingInteraction.PermanentChoice targetChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(targetChoice.validIds()).contains(target.getId()).doesNotContain(protectedCreature.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(protectedCreature);
    }

    private void addEliminatorMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);
    }
}
