package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.cards.o.OnSerrasWings;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiteOfBelzenlok.class, BalothGorger.class, OnSerrasWings.class})
class RiteOfBelzenlokTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Rite of Belzenlok adds a lore counter and triggers chapter I")
    void castingAddsLoreCounterAndTriggersChapterI() {
        harness.setHand(player1, List.of(new RiteOfBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment

        GameData gd = harness.getGameData();

        Permanent saga = findPermanent(player1, "Rite of Belzenlok");
        assertThat(saga).isNotNull();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);

        // Chapter I ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getDescription()).contains("chapter I");
    }

    @Test
    @DisplayName("Chapter I resolving creates two 0/1 black Cleric tokens")
    void chapterICreatesTwoClerics() {
        harness.setHand(player1, List.of(new RiteOfBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers
        harness.passBothPriorities(); // resolve chapter I

        GameData gd = harness.getGameData();

        List<Permanent> clerics = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Cleric") && p.getCard().isToken())
                .toList();
        assertThat(clerics).hasSize(2);
        for (Permanent cleric : clerics) {
            assertThat(cleric.getCard().getPower()).isZero();
            assertThat(cleric.getCard().getToughness()).isEqualTo(1);
            assertThat(cleric.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(cleric.getCard().getSubtypes()).contains(CardSubtype.CLERIC);
        }
    }

    @Test
    @DisplayName("Chapter II creates two more Cleric tokens")
    void chapterIICreatesTwoMoreClerics() {
        harness.addToBattlefield(player1, new RiteOfBelzenlok());
        Permanent saga = findPermanent(player1, "Rite of Belzenlok");
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter II triggers

        GameData gd = harness.getGameData();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter II"));

        harness.passBothPriorities(); // resolve chapter II

        gd = harness.getGameData();

        long clericCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Cleric") && p.getCard().isToken())
                .count();
        assertThat(clericCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Chapter III creates a 6/6 black Demon token with flying and trample")
    void chapterIIICreatesDemonToken() {
        harness.addToBattlefield(player1, new RiteOfBelzenlok());
        Permanent saga = findPermanent(player1, "Rite of Belzenlok");
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers

        GameData gd = harness.getGameData();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(3);

        harness.passBothPriorities(); // resolve chapter III

        gd = harness.getGameData();

        Permanent demon = findPermanent(player1, "Demon");
        assertThat(demon).isNotNull();
        assertThat(demon.getCard().getPower()).isEqualTo(6);
        assertThat(demon.getCard().getToughness()).isEqualTo(6);
        assertThat(demon.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(demon.getCard().getSubtypes()).contains(CardSubtype.DEMON);
        assertThat(demon.getCard().getKeywords()).contains(Keyword.FLYING, Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Demon token deals 6 damage to controller when no other creatures are present")
    void demonDealsDamageWhenNoOtherCreatures() {
        addDemonToken(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 6);
    }

    @Test
    @DisplayName("Demon token sacrifices another creature instead of dealing damage")
    void demonSacrificesOtherCreature() {
        addDemonToken(player1);
        addCreatureReady(player1, new BalothGorger());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Baloth Gorger should be sacrificed
        harness.assertNotOnBattlefield(player1, "Baloth Gorger");
        harness.assertInGraveyard(player1, "Baloth Gorger");
        // No damage dealt
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        // Demon remains
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Demon") && p.getCard().isToken());
    }

    @Test
    @DisplayName("Saga is sacrificed after chapter III resolves")
    void sagaSacrificedAfterChapterIII() {
        harness.addToBattlefield(player1, new RiteOfBelzenlok());
        Permanent saga = findPermanent(player1, "Rite of Belzenlok");
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        GameData gd = harness.getGameData();

        boolean sagaOnBf = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Rite of Belzenlok"));
        assertThat(sagaOnBf).isFalse();

        boolean sagaInGraveyard = gd.playerGraveyards.get(player1.getId()).stream()
                .anyMatch(c -> c.getName().equals("Rite of Belzenlok"));
        assertThat(sagaInGraveyard).isTrue();
    }

    @Test
    @DisplayName("Saga is not sacrificed while chapter III ability is on the stack")
    void sagaNotSacrificedWhileChapterOnStack() {
        harness.addToBattlefield(player1, new RiteOfBelzenlok());
        Permanent saga = findPermanent(player1, "Rite of Belzenlok");
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → lore counter 3, chapter III triggers

        GameData gd = harness.getGameData();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(3);
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }

    @Test
    @DisplayName("The controller chooses another creature to sacrifice when several are available")
    void controllerChoosesSacrifice() {
        Permanent demon = addDemonToken(player1);
        Permanent chosen = addCreatureReady(player1, new BalothGorger());
        Permanent other = addCreatureReady(player1, new BalothGorger());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(demon, other).doesNotContain(chosen);
        harness.assertInGraveyard(player1, "Baloth Gorger");
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("The Demon does not trigger during its opponent's upkeep")
    void demonDoesNotTriggerOnOpponentsUpkeep() {
        addDemonToken(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("An opponent's creature cannot satisfy the Demon's sacrifice")
    void opponentsCreatureCannotBeSacrificed() {
        Permanent demon = addDemonToken(player1);
        Permanent opponentCreature = addCreatureReady(player2, new BalothGorger());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore - 6);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(demon);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
    }

    @Test
    @DisplayName("A Demon with lifelink gains life from its own upkeep damage")
    void demonWithLifelinkGainsLifeFromUpkeepDamage() {
        Permanent demon = addDemonToken(player1);
        harness.setHand(player1, List.of(new OnSerrasWings()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, demon.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, demon, Keyword.LIFELINK)).isTrue();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
    }

    private Permanent addDemonToken(Player player) {
        harness.addToBattlefield(player, new RiteOfBelzenlok());
        Permanent saga = findPermanent(player, "Rite of Belzenlok");
        saga.setCounterCount(CounterType.LORE, 2);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player, "Demon");
    }
}
