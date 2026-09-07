package com.github.laxika.magicalvibes.cards.r;

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

@CardUsed({ResilientRoadrunner.class})
class ResilientRoadrunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Coyote creatures cannot block Resilient Roadrunner")
    void coyoteCannotBlock() {
        addRoadrunner();
        Permanent blocker = addCreature(player2, "Coyote", 3, 3, List.of(CardSubtype.COYOTE));

        prepareBlockingInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from Coyotes prevents their combat damage")
    void coyoteDamageIsPrevented() {
        Permanent coyote = new Permanent(createCreature("Coyote", 3, 3, List.of(CardSubtype.COYOTE)));
        coyote.setSummoningSick(false);
        coyote.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(coyote);

        Permanent roadrunner = new Permanent(new ResilientRoadrunner());
        roadrunner.setSummoningSick(false);
        roadrunner.setBlocking(true);
        roadrunner.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(roadrunner);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

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
        Permanent blocker = addCreature(player2, "Grizzly Bear", 2, 2, List.of());

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

    private Permanent addRoadrunner() {
        Permanent attacker = new Permanent(new ResilientRoadrunner());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);
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
        Permanent permanent = new Permanent(createCreature(name, power, toughness, subtypes, keywords));
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
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
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
