package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CoilingStalker;
import com.github.laxika.magicalvibes.cards.i.InvokeTheWinds;
import com.github.laxika.magicalvibes.cards.n.NeurokTransmuter;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.t.TamiyosCompleation;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({MechtitanCore.class, Ornithopter.class, MindStone.class,
        GrizzlyBears.class, Murder.class, CoilingStalker.class, InvokeTheWinds.class,
        TamiyosCompleation.class, MarchOfOtherworldlyLight.class})
class MechtitanCoreTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles four qualifying permanents and returns them tapped when Mechtitan leaves")
    void createsMechtitanAndReturnsExiledPermanentsTapped() {
        Permanent core = harness.addToBattlefieldAndReturn(player1, new MechtitanCore());
        List<Permanent> ornithopters = List.of(
                harness.addToBattlefieldAndReturn(player1, new Ornithopter()),
                harness.addToBattlefieldAndReturn(player1, new Ornithopter()),
                harness.addToBattlefieldAndReturn(player1, new Ornithopter()),
                harness.addToBattlefieldAndReturn(player1, new Ornithopter())
        );
        Permanent nonCreatureArtifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Permanent nonArtifactCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent mechtitan = findPermanents(player1, "Mechtitan").getFirst();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(
                        core.getCard().getId(),
                        ornithopters.get(0).getCard().getId(),
                        ornithopters.get(1).getCard().getId(),
                        ornithopters.get(2).getCard().getId(),
                        ornithopters.get(3).getCard().getId());

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player1, 0, mechtitan.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mechtitan")).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> ornithopters.stream()
                        .anyMatch(ornithopter -> ornithopter.getCard().getId().equals(p.getCard().getId())))
                .allMatch(Permanent::isTapped);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(nonCreatureArtifact, nonArtifactCreature);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(core.getCard().getId());
    }

    @Test
    @DisplayName("Cannot activate without four artifact creatures or Vehicles")
    void requiresFourQualifyingPermanents() {
        harness.addToBattlefield(player1, new MechtitanCore());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new MindStone());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 4 other artifacts");
    }

    @Test
    void uncrewedVehiclesPayTheCostAndExileReturnsThemTapped() {
        List<Permanent> vehicles = addFiveCores();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(5);
        harness.passBothPriorities();
        Permanent token = findPermanents(player1, "Mechtitan").getFirst();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(10);
        assertThat(gqs.isArtifact(gd, token)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, token)).containsExactlyInAnyOrder(
                CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN);
        for (Keyword keyword : List.of(Keyword.FLYING, Keyword.VIGILANCE, Keyword.TRAMPLE,
                Keyword.LIFELINK, Keyword.HASTE)) {
            assertThat(gqs.hasKeyword(gd, token, keyword)).isTrue();
        }

        exileMechtitan(token);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard().getId())
                .containsExactlyInAnyOrderElementsOf(vehicles.subList(1, 5).stream()
                        .map(p -> p.getCard().getId()).toList());
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(Permanent::isTapped);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).containsExactly(vehicles.getFirst().getCard().getId());
    }

    @Test
    void crewTwoTapsTheCrewAndAnimatesTheCore() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent core = harness.addToBattlefieldAndReturn(player1, new MechtitanCore());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new CoilingStalker());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(core.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, core)).isTrue();
    }

    @Test
    void returnTriggerSurvivesTheTokenLosingItsAbilities() {
        List<Permanent> vehicles = addFiveCores();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent token = findPermanents(player1, "Mechtitan").getFirst();
        harness.setHand(player1, List.of(new TamiyosCompleation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, token.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        exileMechtitan(token);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard().getId())
                .containsExactlyInAnyOrderElementsOf(vehicles.subList(1, 5).stream()
                        .map(p -> p.getCard().getId()).toList());
    }

    @Test
    void changingTokenControllerDoesNotChangeReturnTriggerControllerOrSource() {
        addFiveCores();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent token = findPermanents(player1, "Mechtitan").getFirst();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new InvokeTheWinds()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castSorcery(player2, 0, token.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(token);

        exileMechtitan(token);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(MechtitanCore.class);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @CardUsed({NeurokTransmuter.class})
    void vehicleThatIsNoLongerAnArtifactStillPaysTheExileCost() {
        List<Permanent> vehicles = addFiveCores();
        Permanent transmuter = harness.addToBattlefieldAndReturn(player1, new NeurokTransmuter());
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();
        assertThat(transmuter.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, vehicles.get(1))).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 5, 1, null, vehicles.get(1).getId());
        harness.passBothPriorities();
        assertThat(gqs.isArtifact(gd, vehicles.get(1))).isFalse();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mechtitan");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(5);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(transmuter);
    }

    @Test
    void crewCannotBePaidWithoutTwoPower() {
        harness.addToBattlefield(player1, new MechtitanCore());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.isCreature(gd, gd.playerBattlefields.get(player1.getId()).getFirst())).isFalse();
    }

    private List<Permanent> addFiveCores() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return java.util.stream.IntStream.range(0, 5)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new MechtitanCore()))
                .toList();
    }

    private void exileMechtitan(Permanent token) {
        harness.setHand(player1, List.of(new MarchOfOtherworldlyLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.ensurePriority(player1);
        harness.castInstantForXWithDiscards(player1, 0, 0, List.of(token.getId()), List.of());
        harness.passBothPriorities();
    }
}
