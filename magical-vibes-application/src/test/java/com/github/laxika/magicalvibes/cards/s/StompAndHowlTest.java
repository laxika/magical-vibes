package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StompAndHowl.class, FountainOfYouth.class, GloriousAnthem.class, Ornithopter.class})
class StompAndHowlTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target artifact and a target enchantment")
    void destroysTargetArtifactAndEnchantment() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new StompAndHowl()));
        addMana();

        UUID artifactId = harness.getPermanentId(player2, "Fountain of Youth");
        UUID enchantmentId = harness.getPermanentId(player2, "Glorious Anthem");
        harness.castSorcery(player1, 0, List.of(artifactId, enchantmentId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Destroys the remaining target when the other target becomes illegal")
    void destroysRemainingTargetWhenOtherTargetLeaves() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new StompAndHowl()));
        addMana();

        UUID artifactId = harness.getPermanentId(player2, "Fountain of Youth");
        UUID enchantmentId = harness.getPermanentId(player2, "Glorious Anthem");
        harness.castSorcery(player1, 0, List.of(artifactId, enchantmentId));

        GameData gd = harness.getGameData();
        gd.playerBattlefields.get(player2.getId()).removeIf(permanent -> permanent.getId().equals(artifactId));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Rejects an artifact in the enchantment target position")
    void rejectsWrongTargetType() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new StompAndHowl()));
        addMana();

        UUID artifactId = harness.getPermanentId(player2, "Fountain of Youth");
        UUID secondArtifactId = harness.getPermanentId(player2, "Ornithopter");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(artifactId, secondArtifactId)))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
