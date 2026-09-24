package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LlanowarVanguard;
import com.github.laxika.magicalvibes.cards.v.ViashinoGrappler;
import com.github.laxika.magicalvibes.cards.v.VodalianMerchant;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SabertoothNishoba.class, LlanowarVanguard.class, ViashinoGrappler.class, VodalianMerchant.class})
class SabertoothNishobaTest extends BaseCardTest {

    @Test
    void hasProtectionFromBlueAndRedButNotGreen() {
        harness.addToBattlefield(player1, new SabertoothNishoba());

        Permanent nishoba = findPermanent(player1, "Sabertooth Nishoba");

        assertThat(gqs.hasProtectionFrom(gd, nishoba, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, nishoba, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, nishoba, CardColor.GREEN)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, nishoba, CardColor.WHITE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, nishoba, CardColor.BLACK)).isFalse();
    }

    @Test
    void blueCreatureCannotBlockSabertoothNishoba() {
        Permanent nishoba = addCreatureReady(player1, new SabertoothNishoba());
        nishoba.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new VodalianMerchant());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    void redCreatureCannotBlockSabertoothNishoba() {
        Permanent nishoba = addCreatureReady(player1, new SabertoothNishoba());
        nishoba.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ViashinoGrappler());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    void trampleDealsExcessCombatDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new SabertoothNishoba());
        Permanent blocker = addCreatureReady(player2, new LlanowarVanguard());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 4
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }
}
