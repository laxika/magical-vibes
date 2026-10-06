package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CunningCoyote;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ResilientRoadrunner.class, CunningCoyote.class, RecklessLackey.class})
class ResilientRoadrunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Coyote creatures cannot block Resilient Roadrunner")
    void coyoteCannotBlock() {
        addRoadrunner();
        addCreature(player2, "Coyote", 3, 3, List.of(CardSubtype.COYOTE));

        prepareBlockingInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from Coyotes prevents their combat damage")
    void coyoteDamageIsPrevented() {
        Permanent coyote = addCreatureReady(player1,
                createCreature("Coyote", 3, 3, List.of(CardSubtype.COYOTE)));
        coyote.setAttacking(true);

        Permanent roadrunner = addCreatureReady(player2, new ResilientRoadrunner());
        roadrunner.setBlocking(true);
        roadrunner.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(coyote);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(roadrunner);
    }

    @Test
    @DisplayName("Non-Coyote creatures can block Resilient Roadrunner")
    void nonCoyoteCanBlock() {
        addRoadrunner();
        Permanent blocker = addCreature(player2, "Grizzly Bear", 2, 2, List.of());

        prepareBlockingInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The activated ability allows only creatures with haste to block this turn")
    void activatedAbilityRestrictsBlockersToCreaturesWithHaste() {
        activateRestriction();
        addCreature(player2, "Grizzly Bear", 2, 2, List.of());

        prepareBlockingInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creatures with haste");
    }

    @Test
    @DisplayName("A creature with haste can block after the activated ability resolves")
    void hastyCreatureCanBlock() {
        activateRestriction();
        Permanent blocker = addCreature(player2, "Hasty Bear", 2, 2, List.of(), Keyword.HASTE);

        prepareBlockingInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The activated ability's restriction wears off at end of turn")
    void restrictionWearsOffAtEndOfTurn() {
        activateRestriction();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent blocker = addCreature(player2, "Grizzly Bear", 2, 2, List.of());
        prepareBlockingInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Haste does not let a Coyote bypass protection after activation")
    void hastyCoyoteStillCannotBlock() {
        activateRestriction();
        harness.addToBattlefield(player2, new CunningCoyote());

        prepareBlockingInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("A real non-Coyote with haste can block while summoning sick")
    void recklessLackeyCanBlock() {
        activateRestriction();
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RecklessLackey());

        prepareBlockingInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The restriction can be activated while the source is tapped")
    void tappedRoadrunnerCanActivate() {
        Permanent attacker = addRoadrunner();
        attacker.setTapped(true);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        addCreature(player2, "Grizzly Bear", 2, 2, List.of());

        prepareBlockingInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creatures with haste");
        assertThat(attacker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Haste allows a newly entered Roadrunner to attack")
    void newlyEnteredRoadrunnerCanAttack() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new ResilientRoadrunner());
        harness.addToBattlefield(player2, new RecklessLackey());
        declareAttackers(List.of(0));

        assertThat(attacker.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Protection prevents targeting by a friendly Coyote's triggered ability")
    void friendlyCoyoteCannotTargetRoadrunner() {
        Permanent roadrunner = harness.addToBattlefieldAndReturn(player1, new ResilientRoadrunner());
        harness.setHand(player1, List.of(new CunningCoyote()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, roadrunner.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    private Permanent addRoadrunner() {
        Permanent attacker = addCreatureReady(player1, new ResilientRoadrunner());
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent activateRestriction() {
        Permanent attacker = addRoadrunner();
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        return attacker;
    }

    private Permanent addCreature(Player player, String name,
                                  int power, int toughness, List<CardSubtype> subtypes,
                                  Keyword... keywords) {
        return addCreatureReady(player, createCreature(name, power, toughness, subtypes, keywords));
    }

    private static Card createCreature(String name, int power, int toughness, List<CardSubtype> subtypes,
                                       Keyword... keywords) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(subtypes);
        if (keywords.length > 0) {
            card.setKeywords(Set.of(keywords));
        }
        return card;
    }

    private void prepareBlockingInput() {
        prepareDeclareBlockers();
    }
}
