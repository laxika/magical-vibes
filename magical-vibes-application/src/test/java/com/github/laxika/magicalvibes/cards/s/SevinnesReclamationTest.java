package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SevinnesReclamation.class, GrizzlyBears.class, AirElemental.class, Ornithopter.class})
class SevinnesReclamationTest extends BaseCardTest {

    @Test
    void returnsTargetPermanentWithManaValueThreeOrLessToTheBattlefield() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new SevinnesReclamation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(bears.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Sevinne's Reclamation");
    }

    @Test
    void rejectsPermanentWithManaValueGreaterThanThree() {
        AirElemental elemental = new AirElemental();
        harness.setGraveyard(player1, List.of(elemental));
        harness.setHand(player1, List.of(new SevinnesReclamation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(elemental.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void flashbackMayCopyTheSpellAfterReturningItsTarget() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new SevinnesReclamation(), bears));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveFlashback(player1, 0, bears.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        GameData gameData = harness.getGameData();
        StackEntry copy = gameData.stack.stream()
                .filter(StackEntry::isCopy)
                .findFirst()
                .orElseThrow();
        assertThat(copy.getControllerId()).isEqualTo(player1.getId());
        assertThat(copy.getTargetId()).isEqualTo(bears.getId());

        harness.handleMayAbilityChosen(player1, false);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void flashbackMayDeclineTheCopy() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new SevinnesReclamation(), bears));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveFlashback(player1, 0, bears.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).noneMatch(StackEntry::isCopy);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void flashbackCopyReturnsANewTargetWithoutCreatingAnotherCopy() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new SevinnesReclamation(), first, second));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveFlashback(player1, 0, first.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(first.getId(), second.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exiledCards)
                .anySatisfy(entry -> assertThat(entry.card().getName()).isEqualTo("Sevinne's Reclamation"));
        harness.assertNotInGraveyard(player1, "Sevinne's Reclamation");
    }

    @Test
    void copyKeepingTheReturnedTargetDoesNotReturnItAgain() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new SevinnesReclamation(), bears));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveFlashback(player1, 0, bears.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId()).containsExactly(bears.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void rejectsNonpermanentCardEvenWithManaValueThree() {
        SevinnesReclamation target = new SevinnesReclamation();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new SevinnesReclamation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsPermanentInOpponentsGraveyard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.setHand(player1, List.of(new SevinnesReclamation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void flashbackWithAnIllegalTargetDoesNotOfferACopy() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new SevinnesReclamation(), bears));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFlashback(player1, 0, bears.getId());
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(bears));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exiledCards)
                .anySatisfy(entry -> assertThat(entry.card().getName()).isEqualTo("Sevinne's Reclamation"));
    }

    @Test
    void returnsAZeroManaValueArtifactWithoutOfferingACopyWhenCastFromHand() {
        Ornithopter thopter = new Ornithopter();
        harness.setGraveyard(player1, List.of(thopter));
        harness.setHand(player1, List.of(new SevinnesReclamation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(thopter.getId()));

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertNotInGraveyard(player1, "Ornithopter");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
