package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mortify;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnemyOfTheGuildpact.class, HillGiant.class, Mortify.class, Shock.class, WoollyThoctar.class})
class EnemyOfTheGuildpactTest extends BaseCardTest {

    @Test
    @DisplayName("Has protection from multicolored sources but not monocolored sources")
    void hasProtectionFromMulticoloredSources() {
        Permanent enemy = addCreatureReady(player1, new EnemyOfTheGuildpact());
        Permanent multicoloredSource = addCreatureReady(player2, new WoollyThoctar());
        Permanent monocoloredSource = addCreatureReady(player2, new HillGiant());

        assertThat(gqs.hasProtectionFromSource(gd, enemy, multicoloredSource)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, enemy, monocoloredSource)).isFalse();
    }

    @Test
    @DisplayName("A multicolored spell cannot target Enemy of the Guildpact")
    void multicoloredSpellCannotTarget() {
        Permanent enemy = addCreatureReady(player2, new EnemyOfTheGuildpact());
        harness.setHand(player1, List.of(new Mortify()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enemy.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A monocolored spell can target Enemy of the Guildpact")
    void monocoloredSpellCanTarget() {
        Permanent enemy = addCreatureReady(player2, new EnemyOfTheGuildpact());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, enemy.getId());
        harness.passBothPriorities();

        assertThat(enemy.getMarkedDamage()).isEqualTo(2);
    }
}
