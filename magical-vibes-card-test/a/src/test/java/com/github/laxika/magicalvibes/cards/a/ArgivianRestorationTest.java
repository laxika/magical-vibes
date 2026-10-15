package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.n.NullRod;
import com.github.laxika.magicalvibes.cards.s.SteelGolem;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArgivianRestoration.class, NullRod.class, BenalishInfantry.class, SteelGolem.class})
class ArgivianRestorationTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target artifact card from your graveyard to the battlefield")
    void returnsArtifactFromGraveyardToBattlefield() {
        Card artifact = new NullRod();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new ArgivianRestoration()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, artifact.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(artifact.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(artifact.getId()));
    }

    @Test
    @DisplayName("Cannot target a non-artifact card in the graveyard")
    void cannotTargetNonArtifactCard() {
        Card creature = new BenalishInfantry();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new ArgivianRestoration()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an artifact in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card artifact = new NullRod();
        harness.setGraveyard(player2, List.of(artifact));
        harness.setHand(player1, List.of(new ArgivianRestoration()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    @DisplayName("Fizzles if the target artifact leaves the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyard() {
        Card artifact = new NullRod();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new ArgivianRestoration()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, artifact.getId());
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(artifact.getId()));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Cannot cast without choosing a target even when an artifact is available")
    void cannotCastWithoutTarget() {
        harness.setGraveyard(player1, List.of(new NullRod()));
        harness.setHand(player1, List.of(new ArgivianRestoration()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returns only the selected artifact and leaves other graveyard cards alone")
    void returnsOnlySelectedArtifact() {
        Card selected = new NullRod();
        Card otherArtifact = new NullRod();
        Card creature = new BenalishInfantry();
        harness.setGraveyard(player1, List.of(otherArtifact, creature, selected));
        harness.setHand(player1, List.of(new ArgivianRestoration()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, selected.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(p -> {
                    assertThat(p.getCard().getId()).isEqualTo(selected.getId());
                    assertThat(p.isTapped()).isFalse();
                });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(otherArtifact, creature)
                .doesNotContain(selected);
        harness.assertInGraveyard(player1, "Argivian Restoration");
    }

    @Test
    @DisplayName("Returns artifact creatures without casting them even when creature spells are prohibited")
    void returnsArtifactCreatureWithoutCasting() {
        Card artifactCreature = new SteelGolem();
        harness.addToBattlefield(player1, new SteelGolem());
        harness.setGraveyard(player1, List.of(artifactCreature));
        harness.setHand(player1, List.of(new ArgivianRestoration()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, artifactCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(artifactCreature.getId()) && !p.isTapped());
        harness.assertNotInGraveyard(player1, "Steel Golem");
        harness.assertInGraveyard(player1, "Argivian Restoration");
    }
}
