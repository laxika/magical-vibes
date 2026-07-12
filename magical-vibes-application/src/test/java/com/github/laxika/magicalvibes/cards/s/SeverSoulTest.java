package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BogImp;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeverSoulTest extends BaseCardTest {

    private void giveSpell() {
        harness.setHand(player1, List.of(new SeverSoul()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Destroys a nonblack creature and gains life equal to its toughness")
    void destroysAndGainsLife() {
        Permanent bears = new Permanent(new GrizzlyBears()); // green 2/2
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);

        giveSpell();
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Destroyed creature can't be regenerated")
    void cannotBeRegenerated() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setRegenerationShield(1);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);

        giveSpell();
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        // A valid nonblack target so the spell itself is castable.
        harness.getGameData().playerBattlefields.get(player1.getId()).add(new Permanent(new GrizzlyBears()));

        Permanent imp = new Permanent(new BogImp()); // black creature
        harness.getGameData().playerBattlefields.get(player2.getId()).add(imp);

        giveSpell();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, imp.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
