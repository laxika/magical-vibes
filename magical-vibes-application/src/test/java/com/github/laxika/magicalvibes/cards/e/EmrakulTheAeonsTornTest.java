package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LoxodonMystic;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmrakulTheAeonsTornTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Emrakul gives its controller an extra turn")
    void castingGivesControllerAnExtraTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new EmrakulTheAeonsTorn()));
        harness.addMana(player1, ManaColor.COLORLESS, 15);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    @DisplayName("Emrakul cannot be countered")
    void cannotBeCountered() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        EmrakulTheAeonsTorn emrakul = new EmrakulTheAeonsTorn();
        harness.setHand(player1, List.of(emrakul));
        harness.addMana(player1, ManaColor.COLORLESS, 15);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, emrakul.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Emrakul, the Aeons Torn");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Colored spells cannot target Emrakul, but colored permanent abilities can")
    void protectionOnlyAppliesToColoredSpells() {
        Permanent emrakul = addCreatureReady(player2, new EmrakulTheAeonsTorn());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, emrakul.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from colored spells");

        harness.setHand(player1, List.of());
        Permanent mystic = addCreatureReady(player1, new LoxodonMystic());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, emrakul.getId());
        harness.passBothPriorities();

        assertThat(mystic.isTapped()).isTrue();
        assertThat(emrakul.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking with Emrakul makes the defending player sacrifice six permanents")
    void annihilatorSix() {
        Permanent emrakul = addCreatureReady(player1, new EmrakulTheAeonsTorn());
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(emrakul)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("When Emrakul goes to a graveyard, its owner's graveyard is shuffled into their library")
    void shufflesItsOwnersGraveyardIntoLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        Permanent emrakul = addCreatureReady(player1, new EmrakulTheAeonsTorn());
        emrakul.setMarkedDamage(15);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).extracting(Card::getName)
                .containsExactlyInAnyOrder("Emrakul, the Aeons Torn", "Grizzly Bears");
    }
}
