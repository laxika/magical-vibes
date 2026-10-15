package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FanaticalOffering.class, GrizzlyBears.class, Island.class, Spellbook.class})
class FanaticalOfferingTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature draws two cards and creates a Map token")
    void sacrificesCreatureDrawsTwoAndCreatesMap() {
        Card first = new Spellbook();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FanaticalOffering()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        Permanent map = findPermanents(player1, "Map").getFirst();
        assertThat(map.getCard().isToken()).isTrue();
        assertThat(map.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(map.getCard().getSubtypes()).contains(CardSubtype.MAP);
    }

    @Test
    @DisplayName("Sacrificing an artifact is also a legal additional cost")
    void sacrificesArtifact() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player1, List.of(new FanaticalOffering()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());

        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("Cannot sacrifice a permanent that is neither an artifact nor a creature")
    void rejectsInvalidSacrifice() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new FanaticalOffering()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or creature");
    }

    @Test
    void cannotCastWithoutSacrificing() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FanaticalOffering()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Fanatical Offering");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotSacrificeOpponentsPermanent() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.setHand(player1, List.of(new FanaticalOffering()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Spellbook");
        harness.assertInHand(player1, "Fanatical Offering");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void paysSacrificeBeforeDrawingOrCreatingMap() {
        Card first = new Island();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second));
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player1, List.of(new FanaticalOffering()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());

        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertNotOnBattlefield(player1, "Spellbook");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(countPermanents(player1, "Map")).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(countPermanents(player1, "Map")).isEqualTo(1);
    }

    @Test
    void createdMapExploresControlledCreatureOnlyAtSorcerySpeed() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new FanaticalOffering()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.passBothPriorities();

        Permanent map = findPermanent(player1, "Map");
        int mapIndex = gd.playerBattlefields.get(player1.getId()).indexOf(map);
        Card exploredLand = new Island();
        harness.setLibrary(player1, List.of(exploredLand));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, mapIndex, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Map");

        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        assertThatThrownBy(() -> harness.activateAbility(player1, mapIndex, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Map");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, mapIndex, null, creature.getId());
        harness.assertNotOnBattlefield(player1, "Map");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(exploredLand);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
