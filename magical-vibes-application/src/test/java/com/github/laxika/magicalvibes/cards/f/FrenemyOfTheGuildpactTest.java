package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GloryscaleViashino;
import com.github.laxika.magicalvibes.cards.m.Mortify;
import com.github.laxika.magicalvibes.cards.n.NivixGuildmage;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FrenemyOfTheGuildpact.class, GloryscaleViashino.class, Mortify.class,
        NivixGuildmage.class, WoollyThoctar.class})
class FrenemyOfTheGuildpactTest extends BaseCardTest {

    @Test
    @DisplayName("Has protection from enemy-colored multicolored sources only")
    void hasProtectionFromEnemyColoredMulticoloredSourcesOnly() {
        Permanent frenemy = addCreatureReady(player1, new FrenemyOfTheGuildpact());
        Permanent enemyPair = addCreatureReady(player2, new NivixGuildmage());
        Permanent alliedPair = addCreatureReady(player2, new GloryscaleViashino());
        Permanent threeColor = addCreatureReady(player2, new WoollyThoctar());

        assertThat(gqs.hasProtectionFromSource(gd, frenemy, enemyPair)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, frenemy, alliedPair)).isFalse();
        assertThat(gqs.hasProtectionFromSource(gd, frenemy, threeColor)).isFalse();
    }

    @Test
    @DisplayName("An enemy-colored multicolored spell cannot target Frenemy of the Guildpact")
    void enemyColoredMulticoloredSpellCannotTarget() {
        Permanent frenemy = addCreatureReady(player2, new FrenemyOfTheGuildpact());
        harness.setHand(player1, List.of(new Mortify()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, frenemy.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
