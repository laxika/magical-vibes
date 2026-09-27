package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BriarknitKami;
import com.github.laxika.magicalvibes.cards.g.GhostLitNourisher;
import com.github.laxika.magicalvibes.cards.i.IntoTheFray;
import com.github.laxika.magicalvibes.cards.m.MatsuTribeBirdstalker;
import com.github.laxika.magicalvibes.cards.s.SosukeSonOfSeshiro;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KashiTribeElite.class, SosukeSonOfSeshiro.class, MatsuTribeBirdstalker.class,
        IntoTheFray.class, GhostLitNourisher.class, BriarknitKami.class})
class KashiTribeEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Legendary Snakes you control have shroud")
    void grantsShroudToLegendarySnakesYouControl() {
        Permanent kashi = addCreatureReady(player1, new KashiTribeElite());
        Permanent sosuke = addCreatureReady(player1, new SosukeSonOfSeshiro());
        Permanent ordinarySnake = addCreatureReady(player1, new MatsuTribeBirdstalker());
        Permanent opposingSosuke = addCreatureReady(player2, new SosukeSonOfSeshiro());

        assertThat(gqs.hasKeyword(gd, sosuke, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, kashi, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, ordinarySnake, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingSosuke, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud prevents targeting a legendary Snake")
    void cannotBeTargetedBySpells() {
        addCreatureReady(player1, new KashiTribeElite());
        Permanent sosuke = addCreatureReady(player1, new SosukeSonOfSeshiro());
        harness.setHand(player1, List.of(new IntoTheFray()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, sosuke.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Shroud prevents targeting a legendary Snake with abilities")
    void cannotBeTargetedByAbilities() {
        addCreatureReady(player1, new KashiTribeElite());
        Permanent sosuke = addCreatureReady(player1, new SosukeSonOfSeshiro());
        Permanent nourisher = addCreatureReady(player1, new GhostLitNourisher());
        harness.addMana(player1, ManaColor.GREEN, 3);

        int nourisherIndex = gd.playerBattlefields.get(player1.getId()).indexOf(nourisher);
        assertThatThrownBy(() -> harness.activateAbility(player1, nourisherIndex, null, sosuke.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Combat damage to a creature taps it and locks its next untap step")
    void combatDamageTapsAndLocksDamagedCreature() {
        addCreatureReady(player1, new KashiTribeElite());
        Permanent blocker = addCreatureReady(player2, new BriarknitKami());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(blocker.isTapped()).isTrue();
        assertThat(blocker.getSkipUntapCount()).isEqualTo(1);

        harness.performUntapStep(player2);
        assertThat(blocker.isTapped()).isTrue();
        assertThat(blocker.getSkipUntapCount()).isZero();

        harness.performUntapStep(player2);
        assertThat(blocker.isTapped()).isFalse();
    }
}
