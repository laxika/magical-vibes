package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HandOfEmrakul;
import com.github.laxika.magicalvibes.cards.p.PathrazerOfUlamog;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpawnsireOfUlamogTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking makes the defending player sacrifice a permanent")
    void attackTriggersAnnihilatorOne() {
        Permanent spawnsire = addCreatureReady(player1, new SpawnsireOfUlamog());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(spawnsire)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creates two Eldrazi Spawn tokens that add colorless mana when sacrificed")
    void createsSacrificableEldraziSpawnTokens() {
        addCreatureReady(player1, new SpawnsireOfUlamog());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        List<Permanent> spawns = findPermanents(player1, "Eldrazi Spawn");
        assertThat(spawns).hasSize(2);
        assertThat(spawns).allSatisfy(spawn -> {
            assertThat(spawn.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(spawn.getCard().getSubtypes())
                    .containsExactlyInAnyOrder(CardSubtype.ELDRAZI, CardSubtype.SPAWN);
            assertThat(gqs.getEffectivePower(gd, spawn)).isZero();
            assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(1);
        });

        int spawnIndex = gd.playerBattlefields.get(player1.getId()).indexOf(spawns.getFirst());
        harness.activateAbility(player1, spawnIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(1);
    }

    @Test
    @DisplayName("Offers each outside-the-game Eldrazi spell for a free cast")
    void castsAnyNumberOfEldraziSpellsFromOutsideTheGame() {
        addCreatureReady(player1, new SpawnsireOfUlamog());
        HandOfEmrakul hand = new HandOfEmrakul();
        PathrazerOfUlamog pathrazer = new PathrazerOfUlamog();
        Forest forest = new Forest();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(hand, pathrazer, forest)));
        harness.addMana(player1, ManaColor.COLORLESS, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(forest);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hand of Emrakul");
        harness.assertOnBattlefield(player1, "Pathrazer of Ulamog");
    }
}
