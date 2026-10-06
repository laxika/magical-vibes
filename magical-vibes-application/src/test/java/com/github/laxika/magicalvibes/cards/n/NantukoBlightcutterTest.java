package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CabalTorturer;
import com.github.laxika.magicalvibes.cards.m.MortalCombat;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NantukoBlightcutter.class, CabalTorturer.class, MortalCombat.class})
class NantukoBlightcutterTest extends BaseCardTest {

    @Test
    @DisplayName("Protection from black prevents a black ability from targeting it")
    void hasProtectionFromBlack() {
        Permanent blightcutter = harness.addToBattlefieldAndReturn(player1, new NantukoBlightcutter());
        addCreatureReady(player2, new CabalTorturer());
        harness.addMana(player2, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, blightcutter.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Gets +1/+1 for each black permanent opponents control at threshold")
    void boostsForOpponentsBlackPermanentsAtThreshold() {
        harness.setGraveyard(player1, graveyardCards(7));
        Permanent blightcutter = harness.addToBattlefieldAndReturn(player1, new NantukoBlightcutter());
        harness.addToBattlefield(player1, new CabalTorturer());
        harness.addToBattlefield(player2, new CabalTorturer());
        harness.addToBattlefield(player2, new MortalCombat());

        assertThat(gqs.getEffectivePower(gd, blightcutter)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, blightcutter)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not boost below threshold")
    void doesNotBoostBelowThreshold() {
        harness.setGraveyard(player1, graveyardCards(6));
        Permanent blightcutter = harness.addToBattlefieldAndReturn(player1, new NantukoBlightcutter());
        harness.addToBattlefield(player2, new CabalTorturer());
        harness.addToBattlefield(player2, new CabalTorturer());

        assertThat(gqs.getEffectivePower(gd, blightcutter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blightcutter)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's graveyard does not enable threshold")
    void opponentGraveyardDoesNotEnableThreshold() {
        harness.setGraveyard(player1, graveyardCards(6));
        harness.setGraveyard(player2, graveyardCards(7));
        Permanent blightcutter = harness.addToBattlefieldAndReturn(player1, new NantukoBlightcutter());
        harness.addToBattlefield(player2, new CabalTorturer());

        assertThat(gqs.getEffectivePower(gd, blightcutter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blightcutter)).isEqualTo(2);
    }

    @Test
    @DisplayName("Threshold turns on and off as its controller's graveyard changes")
    void thresholdUpdatesContinuously() {
        Permanent blightcutter = harness.addToBattlefieldAndReturn(player1, new NantukoBlightcutter());
        harness.addToBattlefield(player2, new MortalCombat());
        harness.setGraveyard(player1, graveyardCards(6));

        assertThat(gqs.getEffectivePower(gd, blightcutter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blightcutter)).isEqualTo(2);

        harness.setGraveyard(player1, graveyardCards(7));

        assertThat(gqs.getEffectivePower(gd, blightcutter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blightcutter)).isEqualTo(3);

        harness.setGraveyard(player1, graveyardCards(6));

        assertThat(gqs.getEffectivePower(gd, blightcutter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blightcutter)).isEqualTo(2);
    }

    @Test
    @DisplayName("Nonblack opposing permanents and its controller's black permanents give no bonus")
    void noBonusWithoutOpposingBlackPermanents() {
        harness.setGraveyard(player1, graveyardCards(7));
        Permanent blightcutter = harness.addToBattlefieldAndReturn(player1, new NantukoBlightcutter());
        harness.addToBattlefield(player1, new MortalCombat());
        harness.addToBattlefield(player2, new NantukoBlightcutter());

        assertThat(gqs.getEffectivePower(gd, blightcutter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blightcutter)).isEqualTo(2);

        harness.addToBattlefield(player2, new MortalCombat());

        assertThat(gqs.getEffectivePower(gd, blightcutter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blightcutter)).isEqualTo(3);
    }

    @Test
    @DisplayName("Black creatures cannot block it even below threshold")
    void protectionPreventsBlackBlockers() {
        addCreatureReady(player1, new NantukoBlightcutter());
        addCreatureReady(player2, new CabalTorturer());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    private List<Card> graveyardCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new CabalTorturer());
        }
        return cards;
    }
}
