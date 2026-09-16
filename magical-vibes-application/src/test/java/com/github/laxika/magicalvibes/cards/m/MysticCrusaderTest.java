package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CabalInquisitor;
import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.f.FlameBurst;
import com.github.laxika.magicalvibes.cards.g.GhastlyDemise;
import com.github.laxika.magicalvibes.cards.p.PardicFirecat;
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

@CardUsed({MysticCrusader.class, CabalInquisitor.class, DuskImp.class, FlameBurst.class,
        GhastlyDemise.class, PardicFirecat.class})
class MysticCrusaderTest extends BaseCardTest {

    @Test
    @DisplayName("Has base stats and no flying before threshold")
    void baseStatsBeforeThreshold() {
        harness.addToBattlefield(player1, new MysticCrusader());

        assertStats(2, 1);
        assertThat(gqs.hasKeyword(gd, findCrusader(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+1 and flying with seven cards in its controller's graveyard")
    void thresholdGrantsBoostAndFlying() {
        harness.setGraveyard(player1, graveyardCards(7));
        harness.addToBattlefield(player1, new MysticCrusader());

        assertStats(3, 2);
        assertThat(gqs.hasKeyword(gd, findCrusader(), Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Opponent's graveyard does not enable threshold")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, graveyardCards(7));
        harness.addToBattlefield(player1, new MysticCrusader());

        assertStats(2, 1);
        assertThat(gqs.hasKeyword(gd, findCrusader(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Loses threshold abilities below seven cards")
    void losesThresholdAbilitiesBelowSevenCards() {
        harness.setGraveyard(player1, graveyardCards(7));
        harness.addToBattlefield(player1, new MysticCrusader());
        assertStats(3, 2);
        assertThat(gqs.hasKeyword(gd, findCrusader(), Keyword.FLYING)).isTrue();

        gd.playerGraveyards.get(player1.getId()).removeFirst();

        assertStats(2, 1);
        assertThat(gqs.hasKeyword(gd, findCrusader(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot be targeted by a red spell")
    void cannotBeTargetedByRedSpell() {
        Permanent crusader = addCrusader(player2);
        harness.addToBattlefield(player2, new DuskImp());
        harness.setHand(player1, List.of(new FlameBurst()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, crusader.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Cannot be targeted by a black spell")
    void cannotBeTargetedByBlackSpell() {
        Permanent crusader = addCrusader(player2);
        harness.addToBattlefield(player2, new PardicFirecat());
        harness.setHand(player1, List.of(new GhastlyDemise()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, crusader.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Red creature cannot block Mystic Crusader")
    void redCreatureCannotBlock() {
        addCreatureReady(player1, new MysticCrusader());
        addCreatureReady(player2, new PardicFirecat());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Takes no combat damage from a black creature")
    void takesNoCombatDamageFromBlackCreature() {
        harness.setGraveyard(player1, graveyardCards(7));
        Permanent crusader = addCreatureReady(player1, new MysticCrusader());
        addCreatureReady(player2, new CabalInquisitor());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(crusader.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Mystic Crusader");
        harness.assertNotOnBattlefield(player2, "Cabal Inquisitor");
        harness.assertInGraveyard(player2, "Cabal Inquisitor");
    }

    private List<Card> graveyardCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new DuskImp());
        }
        return cards;
    }

    private Permanent addCrusader(Player player) {
        return harness.addToBattlefieldAndReturn(player, new MysticCrusader());
    }

    private Permanent findCrusader() {
        return findPermanent(player1, "Mystic Crusader");
    }

    private void assertStats(int power, int toughness) {
        Permanent crusader = findCrusader();
        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(toughness);
    }
}
