package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.Gravedigger;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CemeteryReaper.class, Gravedigger.class, RuneclawBear.class, Cancel.class})
class CemeteryReaperTest extends BaseCardTest {

    @Test
    @DisplayName("Other Zombie creatures you control get +1/+1")
    void buffsOwnZombies() {
        harness.addToBattlefield(player1, new Gravedigger());
        harness.addToBattlefield(player1, new CemeteryReaper());

        Permanent gravedigger = findPermanent(player1, "Gravedigger");

        assertThat(gqs.getEffectivePower(gd, gravedigger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gravedigger)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cemetery Reaper does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new CemeteryReaper());

        Permanent reaper = findPermanent(player1, "Cemetery Reaper");

        assertThat(gqs.getEffectivePower(gd, reaper)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, reaper)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff non-Zombie creatures")
    void doesNotBuffNonZombies() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player1, new CemeteryReaper());

        Permanent bears = findPermanent(player1, "Runeclaw Bear");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does NOT buff opponent's Zombie creatures (you control only)")
    void doesNotBuffOpponentZombies() {
        harness.addToBattlefield(player1, new CemeteryReaper());
        harness.addToBattlefield(player2, new Gravedigger());

        Permanent opponentZombie = findPermanent(player2, "Gravedigger");

        assertThat(gqs.getEffectivePower(gd, opponentZombie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentZombie)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Cemetery Reapers buff each other")
    void twoReapersBuffEachOther() {
        harness.addToBattlefield(player1, new CemeteryReaper());
        harness.addToBattlefield(player1, new CemeteryReaper());

        List<Permanent> reapers = findPermanents(player1, "Cemetery Reaper");

        assertThat(reapers).hasSize(2);
        for (Permanent reaper : reapers) {
            assertThat(gqs.getEffectivePower(gd, reaper)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, reaper)).isEqualTo(3);
        }
    }

    @Test
    @DisplayName("Bonus is removed when Cemetery Reaper leaves the battlefield")
    void bonusRemovedWhenReaperLeaves() {
        harness.addToBattlefield(player1, new CemeteryReaper());
        harness.addToBattlefield(player1, new Gravedigger());

        Permanent gravedigger = findPermanent(player1, "Gravedigger");

        assertThat(gqs.getEffectivePower(gd, gravedigger)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Cemetery Reaper"));

        assertThat(gqs.getEffectivePower(gd, gravedigger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gravedigger)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exiles creature card from controller's graveyard and creates 2/2 Zombie token")
    void exilesCreatureAndCreatesToken() {
        Permanent reaper = addCreatureReady(player1, new CemeteryReaper());
        Card bears = new RuneclawBear();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int reaperIndex = gd.playerBattlefields.get(player1.getId()).indexOf(reaper);
        harness.activateAbility(player1, reaperIndex, 0, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        // Creature card exiled from graveyard
        harness.assertNotInGraveyard(player1, "Runeclaw Bear");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Runeclaw Bear"));

        // 2/2 black Zombie token created
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Zombie")
                        && p.getCard().getPower() == 2
                        && p.getCard().getToughness() == 2
                        && p.getCard().getSubtypes().contains(CardSubtype.ZOMBIE));
    }

    @Test
    @DisplayName("Can exile creature card from opponent's graveyard")
    void exilesFromOpponentGraveyard() {
        Permanent reaper = addCreatureReady(player1, new CemeteryReaper());
        Card bears = new RuneclawBear();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(bears));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int reaperIndex = gd.playerBattlefields.get(player1.getId()).indexOf(reaper);
        harness.activateAbility(player1, reaperIndex, 0, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        // Card exiled from opponent's graveyard
        harness.assertNotInGraveyard(player2, "Runeclaw Bear");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Runeclaw Bear"));

        // Token created for controller (player1)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Zombie")
                        && p.getCard().getPower() == 2
                        && p.getCard().getToughness() == 2);
    }

    @Test
    @DisplayName("Rejects non-creature card as target")
    void rejectsNonCreatureTarget() {
        Permanent reaper = addCreatureReady(player1, new CemeteryReaper());
        Card cancel = new Cancel();
        harness.setGraveyard(player1, List.of(cancel));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int reaperIndex = gd.playerBattlefields.get(player1.getId()).indexOf(reaper);

        assertThatThrownBy(() -> harness.activateAbility(player1, reaperIndex, 0, null, cancel.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating ability taps Cemetery Reaper")
    void activatingTapsReaper() {
        Permanent reaper = addCreatureReady(player1, new CemeteryReaper());
        Card bears = new RuneclawBear();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThat(reaper.isTapped()).isFalse();

        int reaperIndex = gd.playerBattlefields.get(player1.getId()).indexOf(reaper);
        harness.activateAbility(player1, reaperIndex, 0, null, bears.getId(), Zone.GRAVEYARD);

        assertThat(reaper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        Permanent reaper = addCreatureReady(player1, new CemeteryReaper());
        Card bears = new RuneclawBear();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int reaperIndex = gd.playerBattlefields.get(player1.getId()).indexOf(reaper);

        assertThatThrownBy(() -> harness.activateAbility(player1, reaperIndex, 0, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent reaper = addCreatureReady(player1, new CemeteryReaper());
        reaper.tap();
        Card bears = new RuneclawBear();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int reaperIndex = gd.playerBattlefields.get(player1.getId()).indexOf(reaper);

        assertThatThrownBy(() -> harness.activateAbility(player1, reaperIndex, 0, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent reaper = harness.addToBattlefieldAndReturn(player1, new CemeteryReaper());
        reaper.setSummoningSick(true);

        Card bears = new RuneclawBear();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int reaperIndex = gd.playerBattlefields.get(player1.getId()).indexOf(reaper);

        assertThatThrownBy(() -> harness.activateAbility(player1, reaperIndex, 0, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if target removed from graveyard before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent reaper = addCreatureReady(player1, new CemeteryReaper());
        Card bears = new RuneclawBear();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int reaperIndex = gd.playerBattlefields.get(player1.getId()).indexOf(reaper);
        harness.activateAbility(player1, reaperIndex, 0, null, bears.getId(), Zone.GRAVEYARD);

        // Remove target before resolution
        gd.playerGraveyards.get(player1.getId()).clear();

        harness.passBothPriorities();

        // No token created since exile fizzled
        harness.assertNotOnBattlefield(player1, "Zombie");
    }

    @Test
    @DisplayName("Created Zombie token gets buffed by Cemetery Reaper's static ability")
    void createdTokenGetsBuffed() {
        Permanent reaper = addCreatureReady(player1, new CemeteryReaper());
        Card bears = new RuneclawBear();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int reaperIndex = gd.playerBattlefields.get(player1.getId()).indexOf(reaper);
        harness.activateAbility(player1, reaperIndex, 0, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent zombieToken = findPermanent(player1, "Zombie");

        // 2/2 base + 1/1 from Cemetery Reaper's lord effect = 3/3
        assertThat(gqs.getEffectivePower(gd, zombieToken)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zombieToken)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability resolves after Cemetery Reaper leaves the battlefield")
    void abilityResolvesAfterSourceLeaves() {
        Permanent reaper = addCreatureReady(player1, new CemeteryReaper());
        Card bear = new RuneclawBear();
        harness.setGraveyard(player2, List.of(bear));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, null, bear.getId(), Zone.GRAVEYARD);
        gd.playerBattlefields.get(player1.getId()).remove(reaper);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Runeclaw Bear");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(bear);
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
        Permanent token = findPermanent(player1, "Zombie");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Zombie");
    }

    @Test
    @DisplayName("Competing activations targeting the same card create only one token")
    void competingActivationsCreateOnlyOneToken() {
        addCreatureReady(player1, new CemeteryReaper());
        addCreatureReady(player1, new CemeteryReaper());
        Card bear = new RuneclawBear();
        harness.setGraveyard(player2, List.of(bear));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.activateAbility(player1, 0, 0, null, bear.getId(), Zone.GRAVEYARD);
        harness.activateAbility(player1, 1, 0, null, bear.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Runeclaw Bear");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(bear);
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
        Permanent token = findPermanent(player1, "Zombie");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
    }

    @Test
    @DisplayName("Exile and token creation wait for ability resolution")
    void effectsWaitForResolution() {
        Permanent reaper = addCreatureReady(player1, new CemeteryReaper());
        Card bear = new RuneclawBear();
        harness.setGraveyard(player1, List.of(bear));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, bear.getId(), Zone.GRAVEYARD);

        assertThat(reaper.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bear);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Zombie");

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bear);
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
    }

}
