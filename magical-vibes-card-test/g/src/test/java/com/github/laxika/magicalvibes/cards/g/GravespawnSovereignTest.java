package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CruelRevival;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GravespawnSovereign.class, GluttonousZombie.class, CruelRevival.class})
class GravespawnSovereignTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping five Zombies puts a creature card from any graveyard onto the battlefield under your control")
    void reanimatesCreatureFromAnyGraveyardUnderYourControl() {
        Permanent sovereign = addCreatureReady(player1, new GravespawnSovereign());
        addZombies(player1, 4);
        Card target = new GluttonousZombie();
        harness.setGraveyard(player2, List.of(target));

        int sovereignIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sovereign);
        harness.activateAbility(player1, sovereignIndex, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(sovereign.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isTapped)
                .count()).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        harness.assertNotInGraveyard(player2, "Gluttonous Zombie");
        harness.assertNotOnBattlefield(player2, "Gluttonous Zombie");
    }

    @Test
    @DisplayName("The ability cannot be activated without five untapped Zombies")
    void requiresFiveUntappedZombies() {
        Permanent sovereign = addCreatureReady(player1, new GravespawnSovereign());
        addZombies(player1, 3);
        Card target = new GluttonousZombie();
        harness.setGraveyard(player2, List.of(target));

        int sovereignIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sovereign);
        assertThatThrownBy(() ->
                harness.activateAbility(player1, sovereignIndex, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped Zombie cannot be used to pay the five-Zombie cost")
    void requiresFiveUntappedZombiesWhenOneZombieIsTapped() {
        Permanent sovereign = addCreatureReady(player1, new GravespawnSovereign());
        addZombies(player1, 4);
        gd.playerBattlefields.get(player1.getId()).get(1).tap();
        Card target = new GluttonousZombie();
        harness.setGraveyard(player2, List.of(target));

        int sovereignIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sovereign);
        assertThatThrownBy(() ->
                harness.activateAbility(player1, sovereignIndex, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot target a noncreature card")
    void cannotTargetNoncreatureCard() {
        Permanent sovereign = addCreatureReady(player1, new GravespawnSovereign());
        addZombies(player1, 4);
        Card target = new CruelRevival();
        harness.setGraveyard(player2, List.of(target));

        int sovereignIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sovereign);
        assertThatThrownBy(() ->
                harness.activateAbility(player1, sovereignIndex, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability fizzles if the targeted creature card leaves the graveyard before resolution")
    void fizzlesWhenTargetLeavesGraveyardBeforeResolution() {
        Permanent sovereign = addCreatureReady(player1, new GravespawnSovereign());
        addZombies(player1, 4);
        Card target = new GluttonousZombie();
        harness.setGraveyard(player2, List.of(target));

        int sovereignIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sovereign);
        harness.activateAbility(player1, sovereignIndex, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        assertThat(sovereign.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Summoning-sick Zombies, including Sovereign, can pay the cost")
    void canTapSummoningSickZombies() {
        Permanent sovereign = harness.addToBattlefieldAndReturn(player1, new GravespawnSovereign());
        sovereign.setSummoningSick(true);
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefieldAndReturn(player1, new GluttonousZombie()).setSummoningSick(true);
        }
        Card target = new GluttonousZombie();
        harness.setGraveyard(player2, List.of(target));

        harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD);

        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(Permanent::isTapped);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        harness.assertNotInGraveyard(player2, "Gluttonous Zombie");
    }

    @Test
    @DisplayName("A tapped Sovereign can activate using five other Zombies and target its own graveyard")
    void tappedSovereignCanReanimateFromOwnGraveyard() {
        Permanent sovereign = addCreatureReady(player1, new GravespawnSovereign());
        sovereign.tap();
        addZombies(player1, 5);
        Card target = new GluttonousZombie();
        harness.setGraveyard(player1, List.of(target));

        harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD);

        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(Permanent::isTapped);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()) && !permanent.isTapped());
        harness.assertNotInGraveyard(player1, "Gluttonous Zombie");
    }

    @Test
    @DisplayName("Opponent's Zombies cannot contribute to the activation cost")
    void cannotTapOpponentsZombies() {
        addCreatureReady(player1, new GravespawnSovereign());
        addZombies(player1, 3);
        addZombies(player2, 5);
        Card target = new GluttonousZombie();
        harness.setGraveyard(player2, List.of(target));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isTapped);
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Removing Sovereign after activation does not stop reanimation")
    void resolvesAfterSovereignLeavesBattlefield() {
        Permanent sovereign = addCreatureReady(player1, new GravespawnSovereign());
        addZombies(player1, 4);
        Card target = new GluttonousZombie();
        harness.setGraveyard(player2, List.of(target));

        harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD);
        gd.playerBattlefields.get(player1.getId()).remove(sovereign);
        harness.setGraveyard(player1, List.of(sovereign.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        harness.assertInGraveyard(player1, "Gravespawn Sovereign");
        harness.assertNotInGraveyard(player2, "Gluttonous Zombie");
    }

    private void addZombies(com.github.laxika.magicalvibes.model.Player player, int count) {
        for (int i = 0; i < count; i++) {
            addCreatureReady(player, new GluttonousZombie());
        }
    }
}
