package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FieldOfRuin;
import com.github.laxika.magicalvibes.cards.f.Frogify;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CrawlingBarrens;
import com.github.laxika.magicalvibes.cards.e.ExpeditionHealer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({ArchonOfEmeria.class, FieldOfRuin.class, Forest.class, GrizzlyBears.class,
        CrawlingBarrens.class, ExpeditionHealer.class, Frogify.class})
class ArchonOfEmeriaTest extends BaseCardTest {

    @Test
    @DisplayName("Each player can cast a first spell but not a second spell")
    void limitsEachPlayerToOneSpell() {
        harness.addToBattlefield(player1, new ArchonOfEmeria());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Archon of Emeria limits an opponent to one spell")
    void limitsOpponentToOneSpell() {
        harness.addToBattlefield(player1, new ArchonOfEmeria());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Opponent's nonbasic lands enter tapped")
    void opponentsNonbasicLandsEnterTapped() {
        harness.addToBattlefield(player1, new ArchonOfEmeria());
        playLand(player2, new FieldOfRuin());

        Permanent land = findPermanent(player2, "Field of Ruin");
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent's basic lands enter untapped")
    void opponentsBasicLandsEnterUntapped() {
        harness.addToBattlefield(player1, new ArchonOfEmeria());
        playLand(player2, new Forest());

        Permanent land = findPermanent(player2, "Forest");
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The controller's nonbasic lands enter untapped")
    void controllersNonbasicLandsEnterUntapped() {
        harness.addToBattlefield(player1, new ArchonOfEmeria());
        playLand(player1, new FieldOfRuin());

        Permanent land = findPermanent(player1, "Field of Ruin");
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    void castingArchonUsesTheOneSpellAllowance() {
        harness.setHand(player1, List.of(new ArchonOfEmeria(), new ExpeditionHealer()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Archon of Emeria");
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void nonbasicLandPutOntoBattlefieldWithoutBeingPlayedEntersTapped() {
        harness.addToBattlefield(player1, new ArchonOfEmeria());

        Permanent land = harness.enterBattlefieldAndReturn(player2, new CrawlingBarrens());

        assertThat(land.isTapped()).isTrue();
    }

    @Test
    void losingAllAbilitiesStopsBothRestrictions() {
        Permanent archon = harness.addToBattlefieldAndReturn(player1, new ArchonOfEmeria());
        harness.setHand(player1, List.of(new Frogify(), new ExpeditionHealer(), new ExpeditionHealer()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castEnchantment(player1, 0, archon.getId());
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof ExpeditionHealer)).hasSize(2);

        playLand(player2, new CrawlingBarrens());
        assertThat(findPermanent(player2, "Crawling Barrens").isTapped()).isFalse();
    }

    private void playLand(com.github.laxika.magicalvibes.model.Player player, com.github.laxika.magicalvibes.model.Card land) {
        harness.setHand(player, List.of(land));
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player, 0);
    }
}
