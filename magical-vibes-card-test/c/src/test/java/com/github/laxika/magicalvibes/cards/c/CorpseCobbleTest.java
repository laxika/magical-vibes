package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HarvesttideSentry;
import com.github.laxika.magicalvibes.cards.u.UnrulyMob;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CorpseCobble.class, Forest.class, CandlegroveWitch.class, HarvesttideSentry.class, UnrulyMob.class})
class CorpseCobbleTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Zombie whose power and toughness equal sacrificed creatures' total power")
    void createsZombieWithTotalSacrificedPower() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player1, new HarvesttideSentry());
        harness.setHand(player1, List.of(new CorpseCobble()));
        addNormalMana();

        harness.castInstantWithSacrifices(player1, 0, null, List.of(bears.getId(), hillGiant.getId()));
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Zombie");
        assertThat(token.getEffectivePower()).isEqualTo(5);
        assertThat(token.getEffectiveToughness()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, token, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Flashback also requires sacrificing creatures and creates the scaled Zombie")
    void flashbackUsesAdditionalCost() {
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player1, new HarvesttideSentry());
        harness.setGraveyard(player1, List.of(new CorpseCobble()));
        addFlashbackMana();

        harness.castFlashbackWithSacrifices(player1, 0, null, List.of(hillGiant.getId()));
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Zombie");
        assertThat(token.getEffectivePower()).isEqualTo(3);
        assertThat(token.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Corpse Cobble"));
    }

    @Test
    @DisplayName("Rejects a noncreature for the additional sacrifice cost")
    void rejectsNoncreatureSacrifice() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new CorpseCobble()));
        addNormalMana();

        assertThatThrownBy(() -> harness.castInstantWithSacrifices(player1, 0, null, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creates exactly one Zombie and pays sacrifices before resolution")
    void createsExactlyOneZombie() {
        Permanent witch = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new HarvesttideSentry());
        harness.setHand(player1, List.of(new CorpseCobble()));
        addNormalMana();

        harness.castInstantWithSacrifices(player1, 0, null, List.of(witch.getId(), sentry.getId()));

        harness.assertNotOnBattlefield(player1, "Candlegrove Witch");
        harness.assertNotOnBattlefield(player1, "Harvesttide Sentry");
        harness.assertInGraveyard(player1, "Candlegrove Witch");
        harness.assertInGraveyard(player1, "Harvesttide Sentry");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(5);
            assertThat(gqs.hasKeyword(gd, token, Keyword.MENACE)).isTrue();
        });
    }

    @Test
    @DisplayName("Flashback creates exactly one Zombie and exiles the spell")
    void flashbackCreatesExactlyOneZombie() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new HarvesttideSentry());
        harness.setGraveyard(player1, List.of(new CorpseCobble()));
        addFlashbackMana();

        harness.castFlashbackWithSacrifices(player1, 0, null, List.of(sentry.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        });
        harness.assertNotInGraveyard(player1, "Corpse Cobble");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Corpse Cobble"));
    }

    @Test
    @DisplayName("Sacrificing zero creatures still creates a Zombie that dies")
    void zeroSacrificesCreatesDyingZombie() {
        Permanent mob = harness.addToBattlefieldAndReturn(player1, new UnrulyMob());
        harness.setHand(player1, List.of(new CorpseCobble()));
        addNormalMana();

        harness.castInstantWithSacrifices(player1, 0, null, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(mob.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(mob);
        harness.assertInGraveyard(player1, "Corpse Cobble");
    }

    @Test
    @DisplayName("Rejects sacrificing an opponent's creature")
    void rejectsOpponentCreatureSacrifice() {
        Permanent witch = harness.addToBattlefieldAndReturn(player2, new CandlegroveWitch());
        harness.setHand(player1, List.of(new CorpseCobble()));
        addNormalMana();

        assertThatThrownBy(() -> harness.castInstantWithSacrifices(player1, 0, null, List.of(witch.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Candlegrove Witch");
        harness.assertInHand(player1, "Corpse Cobble");
    }

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private void addFlashbackMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
