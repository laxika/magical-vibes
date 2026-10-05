package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DevilishValet;
import com.github.laxika.magicalvibes.cards.e.ExhibitionMagician;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OgnisTheDragonsLash.class, ExhibitionMagician.class, DevilishValet.class})
class OgnisTheDragonsLashTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with a creature with haste creates a tapped Treasure")
    void hasteCreatureAttacksCreatesTappedTreasure() {
        addCreatureReady(player1, new OgnisTheDragonsLash());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).hasSize(1);
        assertThat(treasures.getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking with a creature without haste does not create a Treasure")
    void nonHasteCreatureAttacksDoesNotCreateTreasure() {
        addCreatureReady(player1, new OgnisTheDragonsLash());
        addCreatureReady(player1, new ExhibitionMagician());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Only attacking creatures with haste create Treasures")
    void mixedAttackersOnlyHasteCreatureCreatesTreasure() {
        addCreatureReady(player1, new OgnisTheDragonsLash());
        addCreatureReady(player1, new ExhibitionMagician());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Another haste creature triggers Ognis even when Ognis stays back")
    void anotherHasteCreatureAttacks() {
        addCreatureReady(player1, new OgnisTheDragonsLash());
        addCreatureReady(player1, new DevilishValet());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure"))
                .hasSize(1).allMatch(Permanent::isTapped);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Each haste attacker creates its own tapped Treasure")
    void multipleHasteAttackersCreateMultipleTreasures() {
        addCreatureReady(player1, new OgnisTheDragonsLash());
        addCreatureReady(player1, new DevilishValet());
        addCreatureReady(player1, new DevilishValet());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure"))
                .hasSize(3).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("An opponent's haste attacker does not trigger Ognis")
    void opposingHasteAttackerDoesNotTrigger() {
        addCreatureReady(player1, new OgnisTheDragonsLash());
        addCreatureReady(player2, new DevilishValet());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Ognis can attack and trigger on the turn it enters")
    void summoningSickOgnisCanAttack() {
        Permanent ognis = addCreatureReady(player1, new OgnisTheDragonsLash());
        ognis.setSummoningSick(true);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure"))
                .hasSize(1).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("A queued Treasure trigger survives Ognis and the attacker leaving")
    void treasureTriggerSurvivesSourceAndAttackerLeaving() {
        Permanent ognis = addCreatureReady(player1, new OgnisTheDragonsLash());
        Permanent attacker = addCreatureReady(player1, new DevilishValet());

        declareAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).removeAll(List.of(ognis, attacker));
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(ognis.getCard(), attacker.getCard()));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure"))
                .hasSize(1).allMatch(Permanent::isTapped);
    }
}
