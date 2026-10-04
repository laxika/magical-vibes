package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.OrazcaFrillback;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnterTheUnknown.class, Forest.class, OrazcaFrillback.class})
class EnterTheUnknownTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature explores and a land on top goes to hand")
    void targetCreatureExploresLand() {
        Permanent target = addCreature();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        cast(target);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(land.getId());
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Target creature gets a counter when it explores a nonland")
    void targetCreatureExploresNonland() {
        Permanent target = addCreature();
        Card nonland = new OrazcaFrillback();
        harness.setLibrary(player1, List.of(nonland));

        cast(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(nonland.getId());
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new OrazcaFrillback());
        harness.setHand(player1, List.of(new EnterTheUnknown()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void revealedNonlandCanBePutIntoGraveyard() {
        Permanent target = addCreature();
        Card nonland = new OrazcaFrillback();
        harness.setLibrary(player1, List.of(nonland));

        cast(target);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nonland);
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
    }

    @Test
    void emptyLibraryStillGivesCounterAndAdditionalLand() {
        Permanent target = addCreature();
        harness.setLibrary(player1, List.of());

        cast(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void illegalTargetPreventsExploreAndAdditionalLand() {
        Permanent target = addCreature();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of(new EnterTheUnknown()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(1);
        harness.assertInGraveyard(player1, "Enter the Unknown");
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new EnterTheUnknown()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void losingControlOfTargetPreventsEntireSpellFromResolving() {
        Permanent target = addCreature();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of(new EnterTheUnknown()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(1);
        harness.assertInGraveyard(player1, "Enter the Unknown");
    }

    @Test
    void additionalLandCanBePlayedAfterNormalLandAndMultipleSpellsStack() {
        Permanent target = addCreature();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        cast(target);
        harness.playLand(player1, 0);
        harness.setHand(player1, List.of(new Forest()));
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        cast(target);
        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() instanceof Forest).hasSize(3);
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(3);
    }

    private Permanent addCreature() {
        return harness.addToBattlefieldAndReturn(player1, new OrazcaFrillback());
    }

    private void cast(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new EnterTheUnknown()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());
    }
}
