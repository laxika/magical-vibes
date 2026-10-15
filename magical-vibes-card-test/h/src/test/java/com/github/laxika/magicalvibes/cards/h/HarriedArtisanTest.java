package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.p.PhyrexianSkyflayer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HarriedArtisan.class, PhyrexianSkyflayer.class})
class HarriedArtisanTest extends BaseCardTest {

    @Test
    void transformsByPayingWhiteMana() {
        Permanent artisan = addArtisan();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(artisan.isTransformed()).isTrue();
        assertThat(artisan.getCard()).isInstanceOf(PhyrexianSkyflayer.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void canPayPhyrexianManaWithLife() {
        Permanent artisan = addArtisan();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(artisan.isTransformed()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void canOnlyTransformAtSorcerySpeed() {
        addArtisan();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void cannotTransformDuringCombat() {
        addArtisan();
        prepareMainPhase(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void cannotActivateAgainWhileTransformationIsOnStack() {
        Permanent artisan = addArtisan();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(artisan.isTransformed()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        harness.passBothPriorities();
        assertThat(artisan.isTransformed()).isTrue();
    }

    @Test
    void cannotPayPhyrexianManaWithInsufficientLife() {
        Permanent artisan = addArtisan();
        prepareMainPhase(player1);
        harness.setLife(player1, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(artisan.isTransformed()).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedArtisanCanTransformWithoutUntapping() {
        Permanent artisan = addArtisan();
        artisan.tap();
        prepareMainPhase(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(artisan.isTransformed()).isTrue();
        assertThat(artisan.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artisan);
    }

    @Test
    void artisanCanAttackTheTurnItEnters() {
        Permanent artisan = addArtisan();
        artisan.setSummoningSick(true);

        declareAttackers(List.of(0));

        assertThat(artisan.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void skyflayerCanAttackAfterTransformingTheTurnItEnters() {
        Permanent artisan = addArtisan();
        artisan.setSummoningSick(true);
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        assertThat(artisan.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void skyflayerCannotBeBlockedByUntransformedArtisan() {
        Permanent artisan = addArtisan();
        harness.addToBattlefield(player2, new HarriedArtisan());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
        assertThat(artisan.isAttacking()).isTrue();
    }

    private Permanent addArtisan() {
        return harness.addToBattlefieldAndReturn(player1, new HarriedArtisan());
    }

    private void prepareMainPhase(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
