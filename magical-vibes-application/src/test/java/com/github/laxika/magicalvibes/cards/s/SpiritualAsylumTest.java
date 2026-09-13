package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RootwaterCommando;
import com.github.laxika.magicalvibes.cards.t.TerrainGenerator;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiritualAsylum.class, RootwaterCommando.class, TerrainGenerator.class})
class SpiritualAsylumTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures and lands you control have shroud")
    void creaturesAndLandsYouControlHaveShroud() {
        harness.addToBattlefield(player1, new SpiritualAsylum());
        harness.addToBattlefield(player1, new RootwaterCommando());
        harness.addToBattlefield(player1, new TerrainGenerator());
        harness.addToBattlefield(player2, new RootwaterCommando());

        Permanent asylum = findPermanent(player1, "Spiritual Asylum");
        Permanent creature = findPermanent(player1, "Rootwater Commando");
        Permanent land = findPermanent(player1, "Terrain Generator");
        Permanent opponentCreature = findPermanent(player2, "Rootwater Commando");

        assertThat(gqs.hasKeyword(gd, asylum, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Sacrifices itself when a creature you control attacks")
    void sacrificesItselfWhenCreatureAttacks() {
        harness.addToBattlefield(player1, new SpiritualAsylum());
        addCreatureReady(player1, new RootwaterCommando());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Spiritual Asylum");
        harness.assertInGraveyard(player1, "Spiritual Asylum");
    }

    @Test
    @DisplayName("An opponent's creature attacking does not sacrifice it")
    void opponentCreatureAttackingDoesNotSacrificeIt() {
        harness.addToBattlefield(player1, new SpiritualAsylum());
        addCreatureReady(player2, new RootwaterCommando());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spiritual Asylum");
        harness.assertNotInGraveyard(player1, "Spiritual Asylum");
    }
}
