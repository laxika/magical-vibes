package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.Mirari;
import com.github.laxika.magicalvibes.cards.w.WoodlandDruid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DoggedHunter.class, Mirari.class, WoodlandDruid.class})
class DoggedHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Dogged Hunter destroys target creature token")
    void destroysCreatureToken() {
        Permanent hunter = addHunter(player1);
        Permanent token = addCreature(player2, true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, token.getId());

        assertThat(hunter.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Woodland Druid");
    }

    @Test
    @DisplayName("Cannot target a nontoken creature")
    void cannotTargetNontokenCreature() {
        addHunter(player1);
        Permanent creature = addCreature(player2, false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature token")
    void cannotTargetNoncreatureToken() {
        addHunter(player1);
        Permanent token = addNoncreatureToken(player2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, token.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tapping Dogged Hunter can destroy a creature token it controls")
    void destroysOwnCreatureToken() {
        addHunter(player1);
        Permanent token = addCreature(player1, true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, token.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Woodland Druid");
    }

    @Test
    @DisplayName("A summoning-sick Dogged Hunter cannot pay its tap cost")
    void cannotActivateWhileSummoningSick() {
        Permanent hunter = addHunter(player1);
        hunter.setSummoningSick(true);
        Permanent token = addCreature(player2, true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, token.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(hunter.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Dogged Hunter cannot activate")
    void cannotActivateWhileTapped() {
        Permanent hunter = addHunter(player1);
        hunter.setTapped(true);
        Permanent token = addCreature(player2, true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, token.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature token can regenerate from Dogged Hunter's destruction")
    void creatureTokenCanRegenerate() {
        addHunter(player1);
        Permanent token = addCreature(player2, true);
        token.setRegenerationShield(1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, token.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Woodland Druid");
        assertThat(token.getRegenerationShield()).isZero();
        assertThat(token.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Dogged Hunter cannot destroy an indestructible creature token")
    void indestructibleCreatureTokenSurvives() {
        addHunter(player1);
        Permanent token = addCreature(player2, true);
        token.getPersistentGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, token.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Woodland Druid");
        assertThat(token.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability does not resolve against a token that gains hexproof")
    void targetGainingHexproofSurvives() {
        Permanent hunter = addHunter(player1);
        Permanent token = addCreature(player2, true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, token.getId());
        token.getPersistentGrantedKeywords().add(Keyword.HEXPROOF);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Woodland Druid");
        assertThat(hunter.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The activated ability resolves after Dogged Hunter leaves the battlefield")
    void abilityResolvesWithoutSource() {
        Permanent hunter = addHunter(player1);
        Permanent token = addCreature(player2, true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, token.getId());
        gd.playerBattlefields.get(player1.getId()).remove(hunter);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Woodland Druid");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addHunter(Player player) {
        return addCreatureReady(player, new DoggedHunter());
    }

    private Permanent addCreature(Player player, boolean token) {
        Card card = new WoodlandDruid();
        card.setToken(token);
        return harness.addToBattlefieldAndReturn(player, card);
    }

    private Permanent addNoncreatureToken(Player player) {
        Card card = new Mirari();
        card.setToken(true);
        return harness.addToBattlefieldAndReturn(player, card);
    }
}
