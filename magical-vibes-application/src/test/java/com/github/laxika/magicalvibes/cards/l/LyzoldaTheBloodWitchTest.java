package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.EnemyOfTheGuildpact;
import com.github.laxika.magicalvibes.cards.g.GnatAlleyCreeper;
import com.github.laxika.magicalvibes.cards.r.RakdosGuildmage;
import com.github.laxika.magicalvibes.cards.s.SimicRagworm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LyzoldaTheBloodWitch.class, EnemyOfTheGuildpact.class, GnatAlleyCreeper.class,
        RakdosGuildmage.class, SimicRagworm.class})
class LyzoldaTheBloodWitchTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage when the sacrificed creature was red")
    void dealsDamageForRedCreature() {
        addLyzolda();
        Permanent fodder = addCreatureReady(player1, new GnatAlleyCreeper());
        harness.setHand(player1, List.of());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lyzolda, the Blood Witch");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Gnat Alley Creeper");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Deals damage to a creature target when the sacrificed creature was red")
    void dealsDamageToCreatureForRedCreature() {
        addLyzolda();
        Permanent fodder = addCreatureReady(player1, new GnatAlleyCreeper());
        Permanent target = addCreatureReady(player2, new GnatAlleyCreeper());
        harness.setHand(player1, List.of());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lyzolda, the Blood Witch");
        harness.assertInGraveyard(player1, "Gnat Alley Creeper");
        harness.assertInGraveyard(player2, "Gnat Alley Creeper");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Draws a card when the sacrificed creature was black")
    void drawsForBlackCreature() {
        addLyzolda();
        Permanent fodder = addCreatureReady(player1, new EnemyOfTheGuildpact());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new SimicRagworm()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Enemy of the Guildpact");
    }

    @Test
    @DisplayName("Deals damage and draws when the sacrificed creature was red and black")
    void dealsDamageAndDrawsForRedAndBlackCreature() {
        addLyzolda();
        Permanent fodder = addCreatureReady(player1, new RakdosGuildmage());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new SimicRagworm()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Rakdos Guildmage");
    }

    @Test
    @DisplayName("Does nothing extra when the sacrificed creature was neither red nor black")
    void doesNothingForGreenCreature() {
        addLyzolda();
        Permanent fodder = addCreatureReady(player1, new SimicRagworm());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GnatAlleyCreeper()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Simic Ragworm");
    }

    @Test
    @DisplayName("Can sacrifice herself while tapped and summoning sick")
    void sacrificesHerselfForDamageAndDraw() {
        Permanent lyzolda = addCreatureReady(player1, new LyzoldaTheBloodWitch());
        lyzolda.setSummoningSick(true);
        lyzolda.tap();
        addCreatureReady(player1, new SimicRagworm());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new SimicRagworm()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, lyzolda.getId());

        harness.assertInGraveyard(player1, "Lyzolda, the Blood Witch");
        harness.assertNotOnBattlefield(player1, "Lyzolda, the Blood Witch");
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInHand(player1, "Simic Ragworm");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw if the sacrificed creature was also the only target")
    void doesNotDrawWhenSacrificingTheTarget() {
        addLyzolda();
        Permanent fodder = addCreatureReady(player1, new RakdosGuildmage());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new SimicRagworm()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, fodder.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rakdos Guildmage");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Requires a target even when a black creature can be sacrificed")
    void requiresTargetForBlackCreature() {
        addLyzolda();
        addCreatureReady(player1, new EnemyOfTheGuildpact());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Enemy of the Guildpact");
        harness.assertNotInGraveyard(player1, "Enemy of the Guildpact");
        assertThat(gd.stack).isEmpty();
    }

    private void addLyzolda() {
        addCreatureReady(player1, new LyzoldaTheBloodWitch());
    }
}
