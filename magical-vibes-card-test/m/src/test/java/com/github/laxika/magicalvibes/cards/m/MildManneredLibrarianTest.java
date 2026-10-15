package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArcaneAdaptation;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MildManneredLibrarian.class, LlanowarElves.class, ArcaneAdaptation.class})
class MildManneredLibrarianTest extends BaseCardTest {

    @Test
    @DisplayName("Ability makes it a Werewolf, adds two +1/+1 counters, and draws a card")
    void abilityAppliesAllEffects() {
        Permanent librarian = addReadyLibrarian(player1);
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, librarian)).containsExactly(CardSubtype.WEREWOLF);
        assertThat(librarian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Ability can be activated only once for the game")
    void abilityCanBeActivatedOnlyOnce() {
        addReadyLibrarian(player1);
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("Werewolf type remains after the turn ends")
    void werewolfTypeIsPermanent() {
        Permanent librarian = addReadyLibrarian(player1);
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, librarian)).containsExactly(CardSubtype.WEREWOLF);
    }

    @Test
    @DisplayName("Becoming a Werewolf replaces other previously granted creature types")
    void replacesAllPreviousCreatureTypes() {
        Permanent adaptation = harness.addToBattlefieldAndReturn(player1, new ArcaneAdaptation());
        adaptation.setChosenSubtype(CardSubtype.GOBLIN);
        Permanent librarian = addReadyLibrarian(player1);
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, librarian))
                .contains(CardSubtype.HUMAN, CardSubtype.GOBLIN);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, librarian)).containsExactly(CardSubtype.WEREWOLF);
    }

    @Test
    @DisplayName("The activation limit applies before the first activation resolves")
    void cannotActivateAgainWhileAbilityIsOnStack() {
        addReadyLibrarian(player1);
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("The controller still draws when the source leaves before resolution")
    void drawsWhenSourceLeavesBeforeResolution() {
        Permanent librarian = addReadyLibrarian(player1);
        LlanowarElves drawnCard = new LlanowarElves();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(librarian);
        gd.playerGraveyards.get(player1.getId()).add(librarian.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(librarian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A summoning-sick creature may activate this ability")
    void canActivateWhileSummoningSick() {
        Permanent librarian = harness.addToBattlefieldAndReturn(player1, new MildManneredLibrarian());
        librarian.setSummoningSick(true);
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(librarian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private Permanent addReadyLibrarian(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new MildManneredLibrarian());
        perm.setSummoningSick(false);
        return perm;
    }
}
