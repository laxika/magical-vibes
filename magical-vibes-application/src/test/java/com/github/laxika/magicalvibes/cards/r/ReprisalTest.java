package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReprisalTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Reprisal destroys target creature with power 4+ and moves it to graveyard")
    void resolvingDestroysTargetCreature() {
        Permanent wurm = new Permanent(new CrawWurm());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(wurm);

        harness.setHand(player1, List.of(new Reprisal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, wurm.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Craw Wurm"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Craw Wurm"));
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 4")
    void cannotTargetSmallCreature() {
        harness.getGameData().playerBattlefields.get(player1.getId()).add(new Permanent(new CrawWurm()));

        Permanent giant = new Permanent(new HillGiant());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(giant);

        harness.setHand(player1, List.of(new Reprisal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, giant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    @DisplayName("Reprisal does not allow the destroyed creature to regenerate")
    void cannotRegenerate() {
        Permanent wurm = new Permanent(new CrawWurm());
        wurm.setRegenerationShield(1);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(wurm);

        harness.setHand(player1, List.of(new Reprisal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, wurm.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Craw Wurm"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Craw Wurm"));
    }
}
