package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RoyalAssassin;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HaythamKenway.class, BlackKnight.class, GrizzlyBears.class, RoyalAssassin.class, Unsummon.class})
class HaythamKenwayTest extends BaseCardTest {

    @Test
    @DisplayName("Other Knights you control get +2/+2 and protection from Assassins")
    void buffsOtherKnightsAndProtectsThemFromAssassins() {
        harness.addToBattlefield(player1, new HaythamKenway());
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new BlackKnight());
        Permanent assassin = harness.addToBattlefieldAndReturn(player2, new RoyalAssassin());

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(4);
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, knight, assassin)).isTrue();
    }

    @Test
    @DisplayName("Haytham Kenway has protection from Assassins and does not boost itself")
    void protectsItselfAndDoesNotBoostItself() {
        Permanent haytham = harness.addToBattlefieldAndReturn(player1, new HaythamKenway());
        Permanent assassin = harness.addToBattlefieldAndReturn(player2, new RoyalAssassin());

        assertThat(gqs.getEffectivePower(gd, haytham)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, haytham)).isEqualTo(3);
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, haytham, assassin)).isTrue();
    }

    @Test
    @DisplayName("ETB exiles up to one target creature per opponent")
    void exilesTargetCreatureUntilHaythamLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castHaytham(List.of(target.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("The ETB ability can choose no targets")
    void canChooseNoTargets() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castHaytham(List.of());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exiled creatures return when Haytham Kenway leaves")
    void exiledCreatureReturnsWhenHaythamLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castHaytham(List.of(target.getId()));

        UUID haythamId = harness.getPermanentId(player1, "Haytham Kenway");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, haythamId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("The ETB ability cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareHaythamCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castHaytham(List<UUID> targetIds) {
        prepareHaythamCast();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareHaythamCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HaythamKenway()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
