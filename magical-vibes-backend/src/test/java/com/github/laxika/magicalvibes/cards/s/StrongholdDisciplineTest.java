package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.EachPlayerLosesLifePerCreatureControlledEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StrongholdDisciplineTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Stronghold Discipline has correct card properties")
    void hasCorrectProperties() {
        StrongholdDiscipline card = new StrongholdDiscipline();

        assertThat(card.getName()).isEqualTo("Stronghold Discipline");
        assertThat(card.getType()).isEqualTo(CardType.SORCERY);
        assertThat(card.getManaCost()).isEqualTo("{2}{B}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.isNeedsTarget()).isFalse();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(EachPlayerLosesLifePerCreatureControlledEffect.class);
        EachPlayerLosesLifePerCreatureControlledEffect effect =
                (EachPlayerLosesLifePerCreatureControlledEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.lifePerCreature()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting Stronghold Discipline puts it on the stack as a sorcery")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new StrongholdDiscipline()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Stronghold Discipline");
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Each player loses life equal to the number of creatures they control")
    void eachPlayerLosesLifeForOwnCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StrongholdDiscipline()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Stronghold Discipline counts creatures at resolution")
    void countsCreaturesAtResolution() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StrongholdDiscipline()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        gd.playerBattlefields.get(player1.getId()).removeFirst();

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Noncreature permanents are not counted and card goes to graveyard")
    void ignoresNoncreaturesAndGoesToGraveyard() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new StrongholdDiscipline()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Stronghold Discipline"));
    }
}
