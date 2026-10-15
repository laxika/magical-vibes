package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CruelFinality.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class CruelFinalityTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets -2/-2 and the spell's controller scries 1")
    void debuffsTargetAndScries() {
        harness.addToBattlefield(player1, new HillGiant());
        UUID targetId = harness.getPermanentId(player1, "Hill Giant");

        harness.setHand(player1, List.of(new CruelFinality()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getEffectivePower()).isEqualTo(1);
        assertThat(bear.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Scry 1 can bottom the revealed card")
    void scryBottomsTheCard() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new CruelFinality()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        Card originalTop = gd.playerDecks.get(player1.getId()).getFirst();

        harness.castAndResolveInstant(player1, 0, targetId);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.getFirst()).isNotSameAs(originalTop);
        assertThat(deck.getLast()).isSameAs(originalTop);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Cruel Finality");
    }

    @Test
    @DisplayName("The debuff wears off at cleanup")
    void debuffWearsOff() {
        harness.addToBattlefield(player1, new HillGiant());
        UUID targetId = harness.getPermanentId(player1, "Hill Giant");

        harness.setHand(player1, List.of(new CruelFinality()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, targetId);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getEffectivePower()).isEqualTo(3);
        assertThat(bear.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        UUID landId = harness.getPermanentId(player1, "Forest");

        harness.setHand(player1, List.of(new CruelFinality()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Targeting an opponent's creature scries the caster's library and can keep the top card")
    void scriesCasterLibraryAndKeepsTopCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Card top = new Forest();
        Card second = new GrizzlyBears();
        Card opponentTop = new HillGiant();
        harness.setLibrary(player1, List.of(top, second));
        harness.setLibrary(player2, List.of(opponentTop));
        harness.setHand(player1, List.of(new CruelFinality()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.playerId()).isEqualTo(player1.getId());
        assertThat(scry.cards()).containsExactly(top);
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
        harness.assertInGraveyard(player1, "Cruel Finality");
    }

    @Test
    @DisplayName("A creature reduced to zero toughness dies and the caster still scries")
    void lethalDebuffStillScries() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CruelFinality()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Cruel Finality");
    }

    @Test
    @DisplayName("An empty library does not prevent the debuff from resolving")
    void emptyLibraryStillDebuffs() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new CruelFinality()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Cruel Finality");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("If the only target leaves before resolution, the caster does not scry")
    void removedTargetPreventsScry() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card top = new Forest();
        Card second = new HillGiant();
        harness.setLibrary(player1, List.of(top, second));
        harness.setHand(player1, List.of(new CruelFinality(), new CruelFinality()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, second);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }
}
