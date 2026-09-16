package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.g.GhastlyDemise;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MysticEnforcer.class, DuskImp.class, GhastlyDemise.class})
class MysticEnforcerTest extends BaseCardTest {

    @Test
    @DisplayName("Remains 3/3 without threshold")
    void noThresholdBonusBelowSevenCards() {
        fillGraveyard(player1, 6);
        Permanent enforcer = addEnforcer(player1);

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, enforcer, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Gets +3/+3 and flying at threshold")
    void thresholdBonusAtSevenCards() {
        fillGraveyard(player1, 7);
        Permanent enforcer = addEnforcer(player1);

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, enforcer, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Only its controller's graveyard counts")
    void opponentGraveyardDoesNotCount() {
        fillGraveyard(player2, 7);
        Permanent enforcer = addEnforcer(player1);

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, enforcer, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Loses threshold bonus when its controller's graveyard drops below seven cards")
    void losesThresholdBonusWhenGraveyardDropsBelowSevenCards() {
        fillGraveyard(player1, 7);
        Permanent enforcer = addEnforcer(player1);

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, enforcer, Keyword.FLYING)).isTrue();

        gd.playerGraveyards.get(player1.getId()).removeFirst();

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, enforcer, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Protection from black prevents black spells from targeting it")
    void protectionFromBlackPreventsBlackTargeting() {
        Permanent enforcer = addEnforcer(player1);

        harness.setHand(player2, List.of(new GhastlyDemise()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, enforcer.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Protection from black prevents black creatures from blocking it")
    void protectionFromBlackPreventsBlackBlocking() {
        addEnforcer(player1);
        addCreatureReady(player2, new DuskImp());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from black prevents combat damage from black creatures")
    void protectionFromBlackPreventsCombatDamage() {
        fillGraveyard(player1, 7);
        Permanent enforcer = addEnforcer(player1);
        addCreatureReady(player2, new DuskImp());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(enforcer.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Mystic Enforcer");
        harness.assertInGraveyard(player2, "Dusk Imp");
    }

    private Permanent addEnforcer(Player player) {
        return addCreatureReady(player, new MysticEnforcer());
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new DuskImp());
        }
        harness.setGraveyard(player, cards);
    }
}
