package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.n.NeverwinterDryad;
import com.github.laxika.magicalvibes.cards.m.Mordenkainen;
import com.github.laxika.magicalvibes.cards.p.PowerWordKill;
import com.github.laxika.magicalvibes.cards.s.SecretDoor;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VarisSilverymoonRanger.class, NeverwinterDryad.class, SecretDoor.class,
        Mordenkainen.class, PowerWordKill.class})
class VarisSilverymoonRangerTest extends BaseCardTest {

    @Test
    @DisplayName("Ventures when its controller casts a creature spell")
    void venturesWhenControllerCastsCreatureSpell() {
        harness.addToBattlefield(player1, new VarisSilverymoonRanger());
        harness.setHand(player1, List.of(new NeverwinterDryad()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("Triggers only once each turn")
    void triggersOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new VarisSilverymoonRanger());
        harness.setHand(player1, List.of(new NeverwinterDryad(), new NeverwinterDryad()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleListChoice(player1, "Tomb of Annihilation");
        resolveAllTriggers();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.TOMB_OF_ANNIHILATION, 0));
    }

    @Test
    @DisplayName("Creates a Wolf when its controller completes a dungeon")
    void createsWolfOnDungeonCompletion() {
        harness.addToBattlefield(player1, new VarisSilverymoonRanger());
        Permanent door = harness.addToBattlefieldAndReturn(player1, new SecretDoor());
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 4));
        harness.setLibrary(player1, List.of(new NeverwinterDryad()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(door), null, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Wolf")).isEqualTo(1);
        Permanent wolf = findPermanent(player1, "Wolf");
        assertThat(wolf.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, wolf)).containsExactly(CardColor.GREEN);
        assertThat(wolf.getCard().getSubtypes()).containsExactly(CardSubtype.WOLF);
    }

    @Test
    void wardCountersOpponentSpellWhenTheyCannotPayOne() {
        Permanent varis = harness.addToBattlefieldAndReturn(player1, new VarisSilverymoonRanger());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new PowerWordKill()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, varis.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(varis);
        harness.assertInGraveyard(player2, "Power Word Kill");
    }

    @Test
    void planeswalkerCastVenturesAndSharesLimitWithCreatureCasts() {
        harness.addToBattlefield(player1, new VarisSilverymoonRanger());
        harness.setHand(player1, List.of(new Mordenkainen(), new NeverwinterDryad()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castPlaneswalker(player1, 0);
        resolveAllTriggers();
        harness.handleListChoice(player1, "Tomb of Annihilation");
        resolveAllTriggers();
        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.TOMB_OF_ANNIHILATION, 0));

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.TOMB_OF_ANNIHILATION, 0));
    }

    @Test
    void opponentsCreatureCastDoesNotVenture() {
        harness.addToBattlefield(player1, new VarisSilverymoonRanger());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new NeverwinterDryad()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress).isEmpty();
        assertThat(countPermanents(player2, "Neverwinter Dryad")).isEqualTo(1);
    }

    @Test
    void enteringWithoutCastingDoesNotVenture() {
        harness.addToBattlefield(player1, new VarisSilverymoonRanger());
        harness.enterBattlefieldAndReturn(player1, new NeverwinterDryad());
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress).isEmpty();
    }

    @Test
    void opponentsDungeonCompletionDoesNotCreateWolf() {
        harness.addToBattlefield(player1, new VarisSilverymoonRanger());
        harness.addToBattlefield(player2, new SecretDoor());
        harness.forceActivePlayer(player2);
        gd.playerDungeonProgress.put(player2.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 4));
        harness.setLibrary(player2, List.of(new NeverwinterDryad()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
        assertThat(countPermanents(player1, "Wolf")).isZero();
        assertThat(countPermanents(player2, "Wolf")).isZero();
    }

    @Test
    void dungeonCompletionStillCreatesWolfAfterCastTriggerWasUsed() {
        harness.addToBattlefield(player1, new VarisSilverymoonRanger());
        harness.addToBattlefield(player1, new SecretDoor());
        harness.setHand(player1, List.of(new NeverwinterDryad()));
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 1));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLibrary(player1, List.of(new NeverwinterDryad()));

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleListChoice(player1, "Dark Pool");
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Wolf")).isZero();

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Wolf")).isEqualTo(1);
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
    }
}
