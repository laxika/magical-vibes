package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EnigmaDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Power is 0 with an empty graveyard; toughness stays 4")
    void powerZeroWithEmptyGraveyard() {
        Permanent drake = addDrakeReady(player1);

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(4);
    }

    @Test
    @DisplayName("Power equals the number of instant and sorcery cards in your graveyard; toughness stays 4")
    void powerEqualsInstantsAndSorceriesInOwnGraveyard() {
        Permanent drake = addDrakeReady(player1);
        harness.setGraveyard(player1, List.of(new Shock(), new Opt(), new Divination()));

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(4);
    }

    @Test
    @DisplayName("Only instant and sorcery cards count, not other card types")
    void onlyCountsInstantsAndSorceries() {
        Permanent drake = addDrakeReady(player1);

        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new Shock());
        graveyard.add(new Divination());
        graveyard.add(new Plains());
        graveyard.add(new GrizzlyBears());
        harness.setGraveyard(player1, graveyard);

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not count instant and sorcery cards in an opponent's graveyard")
    void doesNotCountOpponentsGraveyard() {
        Permanent drake = addDrakeReady(player1);
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setGraveyard(player2, List.of(new Opt(), new Divination()));

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(4);
    }

    @Test
    @DisplayName("Power updates as instant and sorcery cards enter the graveyard")
    void powerUpdatesWhenSpellsAdded() {
        Permanent drake = addDrakeReady(player1);
        harness.setGraveyard(player1, List.of(new Shock()));

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(1);

        gd.playerGraveyards.get(player1.getId()).add(new Divination());

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(4);
    }

    private Permanent addDrakeReady(Player player) {
        EnigmaDrake card = new EnigmaDrake();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
