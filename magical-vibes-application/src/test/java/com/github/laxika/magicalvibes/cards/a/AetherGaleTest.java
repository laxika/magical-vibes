package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AetherGale.class, GrizzlyBears.class, FountainOfYouth.class, Forest.class})
class AetherGaleTest extends BaseCardTest {

    @Test
    @DisplayName("Returns six target nonland permanents to their owners' hands")
    void returnsSixTargetNonlandPermanents() {
        List<UUID> targets = addFiveCreatures();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        targets.add(artifact.getId());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new AetherGale()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, targets);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(p -> p.getCard().getName())
                .containsExactly("Forest");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Fountain of Youth")
                .filteredOn(name -> name.equals("Grizzly Bears"))
                .hasSize(5);
    }

    @Test
    @DisplayName("Requires exactly six targets")
    void requiresExactlySixTargets() {
        List<UUID> targets = addFiveCreatures();
        harness.setHand(player1, List.of(new AetherGale()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targets))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        List<UUID> targets = new ArrayList<>(addFiveCreatures());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        targets.add(forest.getId());
        harness.setHand(player1, List.of(new AetherGale()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targets))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose the same permanent twice")
    void rejectsDuplicateTargets() {
        List<UUID> targets = addFiveCreatures();
        targets.add(targets.getFirst());
        harness.setHand(player1, List.of(new AetherGale()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targets))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returns permanents controlled by either player to their owners")
    void returnsPermanentsFromBothPlayers() {
        List<UUID> targets = addFiveCreatures();
        targets.add(harness.addToBattlefieldAndReturn(player1, new FountainOfYouth()).getId());
        harness.setHand(player1, List.of(new AetherGale()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, targets);

        harness.assertInHand(player1, "Fountain of Youth");
        harness.assertNotInHand(player2, "Fountain of Youth");
        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .hasSize(5);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Still returns remaining targets when one leaves before resolution")
    void resolvesWithOneMissingTarget() {
        List<UUID> targets = addFiveCreatures();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        targets.add(artifact.getId());
        harness.setHand(player1, List.of(new AetherGale()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, targets);
        harness.getPermanentRemovalService().removePermanentToHand(gd, artifact);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .hasSize(5);
        harness.assertInHand(player2, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Aether Gale");
    }

    @Test
    @DisplayName("Cannot choose more than six targets")
    void rejectsSevenTargets() {
        List<UUID> targets = addFiveCreatures();
        targets.add(harness.addToBattlefieldAndReturn(player2, new FountainOfYouth()).getId());
        targets.add(harness.addToBattlefieldAndReturn(player1, new FountainOfYouth()).getId());
        harness.setHand(player1, List.of(new AetherGale()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targets))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returns a stolen permanent to its owner rather than its controller")
    void returnsStolenPermanentToOwner() {
        List<UUID> targets = addFiveCreatures();
        Permanent stolen = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        gd.stolenCreatures.put(stolen.getId(), player1.getId());
        targets.add(stolen.getId());
        harness.setHand(player1, List.of(new AetherGale()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, targets);

        harness.assertInHand(player1, "Fountain of Youth");
        harness.assertNotInHand(player2, "Fountain of Youth");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    private List<UUID> addFiveCreatures() {
        return IntStream.range(0, 5)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId())
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }
}
