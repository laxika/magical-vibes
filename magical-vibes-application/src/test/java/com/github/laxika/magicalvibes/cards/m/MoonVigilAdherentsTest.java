package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MoonVigilAdherentsTest extends BaseCardTest {

    @Test
    @DisplayName("Counts itself when it is your only creature")
    void countsItself() {
        Permanent adherents = addAdherents(player1);

        assertThat(gqs.getEffectivePower(gd, adherents)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, adherents)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +1/+1 for each creature you control and creature card in your graveyard")
    void countsControlledCreaturesAndGraveyardCreatures() {
        Permanent adherents = addAdherents(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Plains()));

        assertThat(gqs.getEffectivePower(gd, adherents)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, adherents)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not count opposing creatures or noncreature cards")
    void ignoresOpposingCreaturesAndNoncreatureCards() {
        Permanent adherents = addAdherents(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new Plains());
        harness.setGraveyard(player1, graveyard);

        assertThat(gqs.getEffectivePower(gd, adherents)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, adherents)).isEqualTo(1);
    }

    @Test
    @DisplayName("Updates as creatures enter your graveyard")
    void updatesWithGraveyardChanges() {
        Permanent adherents = addAdherents(player1);

        assertThat(gqs.getEffectivePower(gd, adherents)).isEqualTo(1);

        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, adherents)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, adherents)).isEqualTo(2);
    }

    private Permanent addAdherents(Player player) {
        Permanent permanent = new Permanent(new MoonVigilAdherents());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
