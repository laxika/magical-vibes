package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CentaurHealer;
import com.github.laxika.magicalvibes.cards.g.GiftOfOrzhova;
import com.github.laxika.magicalvibes.cards.m.Mortify;
import com.github.laxika.magicalvibes.cards.n.NivixGuildmage;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FrenemyOfTheGuildpact.class, CentaurHealer.class, GiftOfOrzhova.class, TurnToFrog.class, Mortify.class,
        NivixGuildmage.class, WoollyThoctar.class})
class FrenemyOfTheGuildpactTest extends BaseCardTest {

    @Test
    @DisplayName("Has protection from enemy-colored multicolored sources only")
    void hasProtectionFromEnemyColoredMulticoloredSourcesOnly() {
        Permanent frenemy = addCreatureReady(player1, new FrenemyOfTheGuildpact());
        Permanent enemyPair = addCreatureReady(player2, new NivixGuildmage());
        Permanent alliedPair = addCreatureReady(player2, new CentaurHealer());
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

    @Test
    void enemyColoredCreatureCannotBlock() {
        addCreatureReady(player1, new FrenemyOfTheGuildpact());
        addCreatureReady(player2, new NivixGuildmage());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void alliedColoredCreatureCanBlockAndDealDamage() {
        addCreatureReady(player1, new FrenemyOfTheGuildpact());
        addCreatureReady(player2, new CentaurHealer());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Frenemy of the Guildpact");
        harness.assertInGraveyard(player2, "Centaur Healer");
        harness.assertLife(player2, 20);
    }

    @Test
    void enemyColoredCombatDamageIsPrevented() {
        addCreatureReady(player1, new NivixGuildmage());
        Permanent frenemy = addCreatureReady(player2, new FrenemyOfTheGuildpact());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(frenemy.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Frenemy of the Guildpact");
        harness.assertInGraveyard(player1, "Nivix Guildmage");
    }

    @Test
    void enemyColoredHybridAuraCannotEnchantEvenWhenPaidWithOnlyWhiteMana() {
        Permanent frenemy = addCreatureReady(player1, new FrenemyOfTheGuildpact());
        harness.setHand(player1, List.of(new GiftOfOrzhova()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, frenemy.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void losingAllAbilitiesAllowsEnemyColoredSpellToDestroyIt() {
        Permanent frenemy = addCreatureReady(player2, new FrenemyOfTheGuildpact());
        harness.setHand(player1, List.of(new TurnToFrog(), new Mortify()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, frenemy.getId());
        harness.castAndResolveInstant(player1, 0, frenemy.getId());

        harness.assertInGraveyard(player2, "Frenemy of the Guildpact");
    }
}
